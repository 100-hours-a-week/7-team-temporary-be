package molip.server.migration.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import molip.server.outbox.core.model.OutboxMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"migration.enabled"},
        havingValue = "true")
public class MigrationEventApplyService {

    private final EventApplyLogRepository eventApplyLogRepository;
    private final MigrationEventLogRepository migrationEventLogRepository;
    private final MigrationUpsertService migrationUpsertService;
    private final ObjectMapper objectMapper;

    @Transactional(transactionManager = "migrationTransactionManager")
    public boolean applyIfNotExists(OutboxMessage message) {
        if (eventApplyLogRepository.existsByEventId(message.eventId())) {
            return false;
        }
        migrationUpsertService.upsert(message);
        migrationEventLogRepository.save(
                message.eventId(),
                message.aggregateType(),
                message.aggregateId(),
                message.eventType(),
                message.eventVersion(),
                message.occurredAt(),
                toJson(message.payload()));
        eventApplyLogRepository.save(
                message.eventId(),
                message.aggregateType(),
                message.aggregateId(),
                message.eventType(),
                message.eventVersion());
        return true;
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
