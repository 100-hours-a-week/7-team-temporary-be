package molip.server.migration.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.producer.enabled"},
        havingValue = "true")
public class OutboxDlqPublisher {

    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.dlq}")
    private String dlqTopic;

    public void publishDlq(int limit, int maxRetryCount) {
        List<OutboxRecord> records = outboxRepository.findDlqCandidates(limit, maxRetryCount);
        if (records.isEmpty()) {
            return;
        }
        List<Long> dlqIds = new ArrayList<>();
        for (OutboxRecord record : records) {
            if (publish(record)) {
                dlqIds.add(record.id());
            }
        }
        outboxRepository.markDlq(dlqIds);
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
                            record.occurredAt(),
                            SCHEMA_VERSION,
                            payload);
            String messageJson = objectMapper.writeValueAsString(message);
            ProducerRecord<String, String> producerRecord =
                    new ProducerRecord<>(dlqTopic, record.aggregateId(), messageJson);
            producerRecord
                    .headers()
                    .add(
                            "dlqReason",
                            normalizeError(record.lastError()).getBytes(StandardCharsets.UTF_8));
            producerRecord
                    .headers()
                    .add(
                            "failedAt",
                            OffsetDateTime.now(ZoneOffset.UTC)
                                    .toString()
                                    .getBytes(StandardCharsets.UTF_8));
            producerRecord
                    .headers()
                    .add(
                            "retryCount",
                            String.valueOf(record.retryCount()).getBytes(StandardCharsets.UTF_8));
            kafkaTemplate.send(producerRecord).get();
            return true;
        } catch (Exception e) {
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
