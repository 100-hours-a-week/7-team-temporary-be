package molip.server.notification.consumer;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumeIdempotencyService {

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "notification:consume:event:";

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Set<String> localSeen = ConcurrentHashMap.newKeySet();

    public boolean markIfFirst(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return true;
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            Boolean success =
                    redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + eventId, "1", DEFAULT_TTL);
            return Boolean.TRUE.equals(success);
        }
        return localSeen.add(eventId);
    }
}
