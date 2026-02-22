package molip.server.migration.outbox;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.ChangeType;
import molip.server.migration.event.DomainEvent;
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
        DomainEvent event =
                DomainEvent.of(
                        aggregateType.name(),
                        String.valueOf(aggregateId),
                        changeType,
                        resolvedPayload);
        outboxRepository.save(event);
    }
}
