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
        record(aggregateType, aggregateId, ChangeType.CREATED);
    }

    public void recordUpdated(AggregateType aggregateType, Long aggregateId) {
        record(aggregateType, aggregateId, ChangeType.UPDATED);
    }

    public void recordDeleted(AggregateType aggregateType, Long aggregateId) {
        record(aggregateType, aggregateId, ChangeType.DELETED);
    }

    private void record(AggregateType aggregateType, Long aggregateId, ChangeType changeType) {
        if (aggregateType == null || aggregateId == null) {
            return;
        }
        Map<String, Object> payload = Map.of("id", aggregateId);
        DomainEvent event =
                DomainEvent.of(
                        aggregateType.name(), String.valueOf(aggregateId), changeType, payload);
        outboxRepository.save(event);
    }
}
