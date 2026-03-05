package molip.server.chat.redis.idempotency;

import java.time.OffsetDateTime;

public record ChatMessageIdempotencyRecord(
        ChatMessageIdempotencyStatus status, Long messageId, OffsetDateTime sentAt) {}
