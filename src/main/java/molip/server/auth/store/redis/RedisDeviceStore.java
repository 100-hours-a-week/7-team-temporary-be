package molip.server.auth.store.redis;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.auth.store.DeviceStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisDeviceStore implements DeviceStore {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addDevice(Long userId, String deviceId) {
        redisTemplate.opsForSet().add(key(userId), deviceId);
    }

    @Override
    public Set<String> listDevices(Long userId) {
        Set<String> devices = redisTemplate.opsForSet().members(key(userId));
        return devices == null ? Set.of() : devices;
    }

    @Override
    public void clearDevices(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return "auth:devices:" + userId;
    }
}
