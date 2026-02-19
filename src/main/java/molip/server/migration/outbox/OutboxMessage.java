package molip.server.migration.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record OutboxMessage(
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        OffsetDateTime occurredAt,
        int schemaVersion,
        JsonNode payload) {}
