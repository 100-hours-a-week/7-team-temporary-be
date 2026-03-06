package molip.server.auth.store.redis;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.auth.store.RefreshTokenStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(Long userId, String deviceId, String refreshHash) {
        redisTemplate.opsForValue().set(key(userId, deviceId), refreshHash);
    }

    @Override
    public boolean matches(Long userId, String deviceId, String refreshHash) {
        String stored = redisTemplate.opsForValue().get(key(userId, deviceId));
        return stored != null && stored.equals(refreshHash);
    }

    @Override
    public void deleteAll(Long userId, Set<String> deviceIds) {
        for (String deviceId : deviceIds) {
            redisTemplate.delete(key(userId, deviceId));
        }
    }

    private String key(Long userId, String deviceId) {
        return "auth:refresh:" + userId + ":" + deviceId;
    }
}
