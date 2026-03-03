package molip.server.chat.redis.idempotency;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisChatMessageIdempotencyStore {

    private static final Duration TTL = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;

    public Optional<ChatMessageIdempotencyRecord> find(
            Long userId, Long roomId, String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(key(userId, roomId, idempotencyKey));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String[] parts = value.split("\\|", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }

        ChatMessageIdempotencyStatus status = ChatMessageIdempotencyStatus.valueOf(parts[0]);
        Long messageId = parts[1].isBlank() ? null : Long.parseLong(parts[1]);
        OffsetDateTime sentAt = parts[2].isBlank() ? null : OffsetDateTime.parse(parts[2]);

        return Optional.of(new ChatMessageIdempotencyRecord(status, messageId, sentAt));
    }

    public boolean reserve(Long userId, Long roomId, String idempotencyKey) {
        return Boolean.TRUE.equals(
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                key(userId, roomId, idempotencyKey),
                                serialize(ChatMessageIdempotencyStatus.PROCESSING, null, null),
                                TTL));
    }

    public void markSucceeded(
            Long userId,
            Long roomId,
            String idempotencyKey,
            Long messageId,
            OffsetDateTime sentAt) {
        redisTemplate
                .opsForValue()
                .set(
                        key(userId, roomId, idempotencyKey),
                        serialize(ChatMessageIdempotencyStatus.SUCCEEDED, messageId, sentAt),
                        TTL);
    }

    private String key(Long userId, Long roomId, String idempotencyKey) {
        return "chat:message:idempotency:" + userId + ":" + roomId + ":" + idempotencyKey;
    }

    private String serialize(
            ChatMessageIdempotencyStatus status, Long messageId, OffsetDateTime sentAt) {
        String serializedMessageId = messageId == null ? "" : messageId.toString();
        String serializedSentAt = sentAt == null ? "" : sentAt.toString();

        return status.name() + "|" + serializedMessageId + "|" + serializedSentAt;
    }
}
