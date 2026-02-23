package molip.server.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import molip.server.reflection.dto.cache.ReflectionCachePayload;
import molip.server.schedule.dto.cache.DayPlanCachePayload;
import molip.server.user.dto.cache.UserCachePayload;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class RedisReadConsistencyCacheService implements ReadConsistencyCacheService {

    private static final Duration TTL = Duration.ofSeconds(5);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisReadConsistencyCacheService(
            StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cacheUser(UserCachePayload payload) {
        putAfterCommit(
                CacheKey.user(payload.userId()),
                payload.version(),
                payload,
                UserCachePayload.class);
    }

    @Override
    public Optional<UserCachePayload> getUser(Long userId) {
        return get(CacheKey.user(userId), UserCachePayload.class);
    }

    @Override
    public void evictUser(Long userId) {
        deleteAfterCommit(CacheKey.user(userId));
    }

    @Override
    public void cacheReflection(ReflectionCachePayload payload) {
        putAfterCommit(
                CacheKey.reflection(payload.reflectionId()),
                payload.version(),
                payload,
                ReflectionCachePayload.class);
    }

    @Override
    public Optional<ReflectionCachePayload> getReflection(Long reflectionId) {
        return get(CacheKey.reflection(reflectionId), ReflectionCachePayload.class);
    }

    @Override
    public void evictReflection(Long reflectionId) {
        deleteAfterCommit(CacheKey.reflection(reflectionId));
    }

    @Override
    public void cacheDayPlan(DayPlanCachePayload payload) {
        putAfterCommit(
                CacheKey.dayPlan(payload.dayPlanId()),
                payload.version(),
                payload,
                DayPlanCachePayload.class);
    }

    @Override
    public Optional<DayPlanCachePayload> getDayPlan(Long dayPlanId) {
        return get(CacheKey.dayPlan(dayPlanId), DayPlanCachePayload.class);
    }

    @Override
    public void evictDayPlan(Long dayPlanId) {
        deleteAfterCommit(CacheKey.dayPlan(dayPlanId));
    }

    private <T> Optional<T> get(String key, Class<T> payloadType) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            JavaType type =
                    objectMapper
                            .getTypeFactory()
                            .constructParametricType(CacheEntry.class, payloadType);
            CacheEntry<T> entry = objectMapper.readValue(raw, type);
            return Optional.ofNullable(entry.payload());
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    private <T> void putAfterCommit(String key, long version, T payload, Class<T> payloadType) {
        runAfterCommit(() -> putIfNewer(key, version, payload, payloadType));
    }

    private void deleteAfterCommit(String key) {
        runAfterCommit(() -> redisTemplate.delete(key));
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

    private <T> void putIfNewer(String key, long version, T payload, Class<T> payloadType) {
        Optional<CacheEntry<T>> existing = getEntry(key, payloadType);
        if (existing.isPresent() && existing.get().version() >= version) {
            return;
        }
        try {
            String value = objectMapper.writeValueAsString(new CacheEntry<>(version, payload));
            redisTemplate.opsForValue().set(key, value, TTL);
        } catch (JsonProcessingException ignored) {
        }
    }

    private <T> Optional<CacheEntry<T>> getEntry(String key, Class<T> payloadType) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            JavaType type =
                    objectMapper
                            .getTypeFactory()
                            .constructParametricType(CacheEntry.class, payloadType);
            CacheEntry<T> entry = objectMapper.readValue(raw, type);
            return Optional.ofNullable(entry);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
