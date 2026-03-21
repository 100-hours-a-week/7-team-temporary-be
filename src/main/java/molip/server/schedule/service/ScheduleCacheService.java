package molip.server.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.common.response.PageResponse;
import molip.server.schedule.dto.response.DayPlanScheduleExistResponse;
import molip.server.schedule.dto.response.DayPlanTodoListResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleCacheService {

    private static final Duration TODO_LIST_TTL = Duration.ofSeconds(90);
    private static final Duration EXCLUDED_TTL = Duration.ofSeconds(90);
    private static final Duration PERIOD_TTL = Duration.ofSeconds(240);

    private static final int TTL_JITTER_PERCENT = 15;
    private static final int HOT_KEY_WINDOW_MINUTES = 5;
    private static final int HOT_KEY_TOP_N = 10;
    private static final long HOT_KEY_REFRESH_MS = 10_000L;
    private static final int DISTRIBUTED_LOCK_RETRY_MAX = 3;
    private static final long DISTRIBUTED_LOCK_WAIT_MS = 100L;
    private static final Duration DISTRIBUTED_LOCK_TTL = Duration.ofSeconds(3);
    private static final long METRIC_LOG_INTERVAL_MS = 60_000L;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, LongAdder> keyHits = new ConcurrentHashMap<>();
    private final LongAdder cacheRequestCount = new LongAdder();
    private final LongAdder cacheHitCount = new LongAdder();
    private final LongAdder cacheMissCount = new LongAdder();
    private final LongAdder cacheEvictCount = new LongAdder();
    private final LongAdder cacheFallbackCount = new LongAdder();
    private final LongAdder lockAcquireCount = new LongAdder();
    private final LongAdder lockWaitHitCount = new LongAdder();

    private volatile long windowStartedAtMillis = System.currentTimeMillis();
    private volatile long hotKeyRecomputedAtMillis = 0L;
    private volatile long lastMetricLoggedAtMillis = System.currentTimeMillis();
    private volatile Set<String> hotKeys = Set.of();

    public DayPlanTodoListResponse getTodoList(
            Long userId,
            Long dayPlanId,
            int page,
            int size,
            Supplier<DayPlanTodoListResponse> loader) {

        String key = todoListKey(userId, dayPlanId, page, size);
        JavaType type = objectMapper.getTypeFactory().constructType(DayPlanTodoListResponse.class);
        return getOrLoad(key, type, TODO_LIST_TTL, loader);
    }

    public PageResponse<ScheduleSummaryResponse> getExcludedList(
            Long userId,
            Long dayPlanId,
            String status,
            int page,
            int size,
            Supplier<PageResponse<ScheduleSummaryResponse>> loader) {

        String key = excludedKey(userId, dayPlanId, status, page, size);
        JavaType type =
                objectMapper
                        .getTypeFactory()
                        .constructParametricType(PageResponse.class, ScheduleSummaryResponse.class);
        return getOrLoad(key, type, EXCLUDED_TTL, loader);
    }

    public DayPlanScheduleExistResponse getPeriodExistence(
            Long userId,
            String startDate,
            String endDate,
            Supplier<DayPlanScheduleExistResponse> loader) {

        String key = periodKey(userId, startDate, endDate);
        JavaType type =
                objectMapper.getTypeFactory().constructType(DayPlanScheduleExistResponse.class);
        return getOrLoad(key, type, PERIOD_TTL, loader);
    }

    public void evictUserScheduleCaches(Long userId) {
        runAfterCommit(
                () -> {
                    deleteByPrefix("schedule:list:user:" + userId + ":");
                    deleteByPrefix("schedule:excluded:user:" + userId + ":");
                    deleteByPrefix("schedule:period:user:" + userId + ":");
                    cacheEvictCount.increment();
                    logMetricsIfNeeded();
                });
    }

    private <T> T getOrLoad(String key, JavaType javaType, Duration ttl, Supplier<T> loader) {
        cacheRequestCount.increment();
        rotateWindowIfNeeded();
        countHit(key);
        refreshHotKeysIfNeeded();

        T cached = readCache(key, javaType);
        if (cached != null) {
            cacheHitCount.increment();
            logMetricsIfNeeded();
            return cached;
        }
        cacheMissCount.increment();

        if (!hotKeys.contains(key)) {
            T loaded = loadAndCache(key, javaType, ttl, loader);
            logMetricsIfNeeded();
            return loaded;
        }

        T loaded = loadWithDistributedLock(key, javaType, ttl, loader);
        logMetricsIfNeeded();
        return loaded;
    }

    private <T> T loadWithDistributedLock(
            String key, JavaType javaType, Duration ttl, Supplier<T> loader) {
        String lockKey = lockKey(key);
        String lockToken = UUID.randomUUID().toString();

        if (tryAcquireLock(lockKey, lockToken)) {
            lockAcquireCount.increment();
            try {
                return loadAndCache(key, javaType, ttl, loader);
            } finally {
                releaseLock(lockKey, lockToken);
            }
        }

        for (int i = 0; i < DISTRIBUTED_LOCK_RETRY_MAX; i++) {
            sleepQuietly(DISTRIBUTED_LOCK_WAIT_MS);
            T cached = readCache(key, javaType);
            if (cached != null) {
                lockWaitHitCount.increment();
                return cached;
            }
        }

        cacheFallbackCount.increment();
        return loadAndCache(key, javaType, ttl, loader);
    }

    private boolean tryAcquireLock(String lockKey, String lockToken) {
        Boolean acquired =
                redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, DISTRIBUTED_LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    private void releaseLock(String lockKey, String lockToken) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then "
                        + "return redis.call('del', KEYS[1]) "
                        + "else return 0 end");
        redisTemplate.execute(script, List.of(lockKey), lockToken);
    }

    private <T> T loadAndCache(String key, JavaType javaType, Duration ttl, Supplier<T> loader) {
        T loaded = loader.get();
        if (loaded == null) {
            return null;
        }

        try {
            String value = objectMapper.writeValueAsString(loaded);
            redisTemplate.opsForValue().set(key, value, withJitter(ttl));
        } catch (JsonProcessingException ignored) {
            // Ignore cache serialization failure and return DB result.
        }
        return loaded;
    }

    private <T> T readCache(String key, JavaType javaType) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(raw, javaType);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    private Duration withJitter(Duration baseTtl) {
        long baseSeconds = Math.max(1L, baseTtl.getSeconds());
        long min = Math.max(1L, baseSeconds * (100 - TTL_JITTER_PERCENT) / 100);
        long max = Math.max(min, baseSeconds * (100 + TTL_JITTER_PERCENT) / 100);
        long jittered = ThreadLocalRandom.current().nextLong(min, max + 1L);
        return Duration.ofSeconds(jittered);
    }

    private void countHit(String key) {
        keyHits.computeIfAbsent(key, ignored -> new LongAdder()).increment();
    }

    private synchronized void rotateWindowIfNeeded() {
        long now = System.currentTimeMillis();
        long windowMillis = Duration.ofMinutes(HOT_KEY_WINDOW_MINUTES).toMillis();
        if (now - windowStartedAtMillis < windowMillis) {
            return;
        }
        keyHits.clear();
        hotKeys = Set.of();
        windowStartedAtMillis = now;
        hotKeyRecomputedAtMillis = now;
    }

    private synchronized void refreshHotKeysIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - hotKeyRecomputedAtMillis < HOT_KEY_REFRESH_MS) {
            return;
        }

        List<String> topKeys =
                keyHits.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()))
                        .limit(HOT_KEY_TOP_N)
                        .map(entry -> entry.getKey())
                        .toList();

        hotKeys = Set.copyOf(topKeys);
        hotKeyRecomputedAtMillis = now;
    }

    private void deleteByPrefix(String prefix) {
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(1000).build();
        List<String> keys = new ArrayList<>();

        redisTemplate.execute(
                (RedisConnection connection) -> {
                    try (Cursor<byte[]> cursor = connection.scan(options)) {
                        while (cursor.hasNext()) {
                            keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                        }
                    } catch (Exception ignored) {
                        // Ignore scan failure for cache invalidation safety.
                    }
                    return null;
                });

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void runAfterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runnable.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        runnable.run();
                    }
                });
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String lockKey(String key) {
        return "schedule:cache:lock:" + key;
    }

    private void logMetricsIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastMetricLoggedAtMillis < METRIC_LOG_INTERVAL_MS) {
            return;
        }
        synchronized (this) {
            if (now - lastMetricLoggedAtMillis < METRIC_LOG_INTERVAL_MS) {
                return;
            }
            long request = cacheRequestCount.sum();
            long hit = cacheHitCount.sum();
            long miss = cacheMissCount.sum();
            long evict = cacheEvictCount.sum();
            long fallback = cacheFallbackCount.sum();
            long lockAcquired = lockAcquireCount.sum();
            long lockWaitHit = lockWaitHitCount.sum();
            double hitRatio = request == 0 ? 0.0 : (hit * 100.0 / request);

            log.info(
                    "CACHE_METRIC schedule request={} hit={} miss={} hitRatio={} evict={} fallback={} lockAcquired={} lockWaitHit={}",
                    request,
                    hit,
                    miss,
                    String.format("%.2f", hitRatio),
                    evict,
                    fallback,
                    lockAcquired,
                    lockWaitHit);
            lastMetricLoggedAtMillis = now;
        }
    }

    private String todoListKey(Long userId, Long dayPlanId, int page, int size) {
        return "schedule:list:user:"
                + userId
                + ":dayPlan:"
                + dayPlanId
                + ":page:"
                + page
                + ":size:"
                + size;
    }

    private String excludedKey(Long userId, Long dayPlanId, String status, int page, int size) {
        return "schedule:excluded:user:"
                + userId
                + ":dayPlan:"
                + dayPlanId
                + ":status:"
                + status
                + ":page:"
                + page
                + ":size:"
                + size;
    }

    private String periodKey(Long userId, String startDate, String endDate) {
        return "schedule:period:user:" + userId + ":start:" + startDate + ":end:" + endDate;
    }
}
