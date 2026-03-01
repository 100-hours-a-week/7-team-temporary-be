package molip.server.auth.store.redis;

import lombok.RequiredArgsConstructor;
import molip.server.auth.store.TokenVersionStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenVersionStore implements TokenVersionStore {

    private static final long DEFAULT_VERSION = 1L;

    private final StringRedisTemplate redisTemplate;

    @Override
    public long getOrInit(Long userId) {
        redisTemplate.opsForValue().setIfAbsent(key(userId), String.valueOf(DEFAULT_VERSION));
        return get(userId);
    }

    @Override
    public long get(Long userId) {
        String stored = redisTemplate.opsForValue().get(key(userId));
        if (stored == null || stored.isBlank()) {
            return DEFAULT_VERSION;
        }
        return Long.parseLong(stored);
    }

    @Override
    public long increment(Long userId) {
        Long updated = redisTemplate.opsForValue().increment(key(userId));
        if (updated == null) {
            redisTemplate.opsForValue().set(key(userId), String.valueOf(DEFAULT_VERSION));
            return DEFAULT_VERSION;
        }
        return updated;
    }

    private String key(Long userId) {
        return "auth:token-version:" + userId;
    }
}
