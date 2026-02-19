package molip.server.migration.outbox;

import java.time.OffsetDateTime;

public record OutboxRecord(
        Long id,
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        OffsetDateTime occurredAt,
        String payload,
        OutboxStatus status,
        int retryCount,
        String lastError,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}
