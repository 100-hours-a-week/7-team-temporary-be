package molip.server.outbox.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record OutboxMessage(
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        long eventVersion,
        OffsetDateTime occurredAt,
        int schemaVersion,
        JsonNode payload) {}
