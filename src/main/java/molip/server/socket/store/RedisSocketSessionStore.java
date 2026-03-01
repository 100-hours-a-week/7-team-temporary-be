package molip.server.socket.store;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSocketSessionStore {

    private final StringRedisTemplate redisTemplate;

    public void save(String sessionId, Long userId, String deviceId, OffsetDateTime connectedAt) {
        String previousSessionId = redisTemplate.opsForValue().get(userDeviceKey(userId, deviceId));
        if (previousSessionId != null && !previousSessionId.equals(sessionId)) {
            redisTemplate.delete(sessionKey(previousSessionId));
        }

        String value = userId + "|" + deviceId + "|" + connectedAt.toString();

        redisTemplate.opsForValue().set(sessionKey(sessionId), value);
        redisTemplate.opsForValue().set(userDeviceKey(userId, deviceId), sessionId);
    }

    public void delete(String sessionId, Long userId, String deviceId) {
        redisTemplate.delete(sessionKey(sessionId));
        redisTemplate.delete(userDeviceKey(userId, deviceId));
    }

    public void touch(String sessionId) {
        String value = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (value == null) {
            return;
        }

        String[] parts = value.split("\\|", -1);
        if (parts.length != 3) {
            return;
        }

        String touched =
                parts[0] + "|" + parts[1] + "|" + OffsetDateTime.now(ZoneOffset.UTC).toString();
        redisTemplate.opsForValue().set(sessionKey(sessionId), touched);
    }

    private String sessionKey(String sessionId) {
        return "socket:session:" + sessionId;
    }

    private String userDeviceKey(Long userId, String deviceId) {
        return "socket:user-device:" + userId + ":" + deviceId;
    }
}
