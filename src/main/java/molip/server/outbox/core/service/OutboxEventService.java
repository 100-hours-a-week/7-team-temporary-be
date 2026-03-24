package molip.server.outbox.core.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.ChangeType;
import molip.server.migration.event.DomainEvent;
import molip.server.outbox.core.repository.OutboxRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxRepository outboxRepository;

    public void recordCreated(AggregateType aggregateType, Long aggregateId) {
        record(aggregateType, aggregateId, ChangeType.CREATED, null);
    }

    public void recordUpdated(AggregateType aggregateType, Long aggregateId) {
        record(aggregateType, aggregateId, ChangeType.UPDATED, null);
    }

    public void recordDeleted(AggregateType aggregateType, Long aggregateId) {
        record(aggregateType, aggregateId, ChangeType.DELETED, null);
    }

    public void recordCreated(
            AggregateType aggregateType, Long aggregateId, Map<String, Object> payload) {
        record(aggregateType, aggregateId, ChangeType.CREATED, payload);
    }

    public void recordUpdated(
            AggregateType aggregateType, Long aggregateId, Map<String, Object> payload) {
        record(aggregateType, aggregateId, ChangeType.UPDATED, payload);
    }

    public void recordDeleted(
            AggregateType aggregateType, Long aggregateId, Map<String, Object> payload) {
        record(aggregateType, aggregateId, ChangeType.DELETED, payload);
    }

    private void record(
            AggregateType aggregateType,
            Long aggregateId,
            ChangeType changeType,
            Map<String, Object> payload) {
        if (aggregateType == null || aggregateId == null) {
            return;
        }
        Map<String, Object> resolvedPayload = payload == null ? Map.of("id", aggregateId) : payload;
        long eventVersion = resolveEventVersion(resolvedPayload);
        DomainEvent event =
                DomainEvent.of(
                        aggregateType.name(),
                        String.valueOf(aggregateId),
                        changeType,
                        eventVersion,
                        resolvedPayload);
        outboxRepository.save(event);
    }

    private long resolveEventVersion(Map<String, Object> payload) {
        Object version = payload.get("version");
        if (version instanceof Number number) {
            return number.longValue();
        }
        if (version instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }
}
