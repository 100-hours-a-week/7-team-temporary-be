package molip.server.migration.event;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventId,
        String aggregateType,
        String aggregateId,
        ChangeType eventType,
        OffsetDateTime occurredAt,
        int schemaVersion,
        Map<String, Object> payload) {

    public static final int SCHEMA_VERSION_V1 = 1;

    public static DomainEvent of(
            String aggregateType,
            String aggregateId,
            ChangeType eventType,
            Map<String, Object> payload) {
        return new DomainEvent(
                UUID.randomUUID().toString(),
                aggregateType,
                aggregateId,
                eventType,
                OffsetDateTime.now(ZoneOffset.UTC),
                SCHEMA_VERSION_V1,
                payload);
    }
}
