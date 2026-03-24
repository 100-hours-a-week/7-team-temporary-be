package molip.server.outbox.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.outbox.core.model.OutboxMessage;
import molip.server.outbox.core.model.OutboxRecord;
import molip.server.outbox.core.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.producer.enabled"},
        havingValue = "true")
public class OutboxPublisher {

    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.domain-events}")
    private String domainEventsTopic;

    public void publishPending(int limit) {
        List<OutboxRecord> records = outboxRepository.findPending(limit);
        if (records.isEmpty()) {
            return;
        }
        List<Long> sentIds = new ArrayList<>();
        for (OutboxRecord record : records) {
            if (publish(record)) {
                sentIds.add(record.id());
            }
        }
        outboxRepository.markSent(sentIds);
    }

    private boolean publish(OutboxRecord record) {
        try {
            JsonNode payload = objectMapper.readTree(record.payload());
            OutboxMessage message =
                    new OutboxMessage(
                            record.eventId(),
                            record.aggregateType(),
                            record.aggregateId(),
                            record.eventType(),
                            record.eventVersion(),
                            record.occurredAt(),
                            SCHEMA_VERSION,
                            payload);
            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(domainEventsTopic, record.aggregateId(), messageJson).get();
            return true;
        } catch (Exception e) {
            outboxRepository.markFailed(record.id(), normalizeError(e.getMessage()));
            return false;
        }
    }

    private String normalizeError(String message) {
        if (message == null || message.isBlank()) {
            return "unknown_error@" + OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_LENGTH);
    }
}
