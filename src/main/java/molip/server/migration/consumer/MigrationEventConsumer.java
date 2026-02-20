package molip.server.migration.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import molip.server.migration.outbox.OutboxMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {
            "kafka.enabled",
            "kafka.consumer.enabled",
            "migration.enabled",
            "migration.datasource.url"
        },
        havingValue = "true")
public class MigrationEventConsumer {

    private final ObjectMapper objectMapper;
    private final MigrationEventApplyService migrationEventApplyService;

    @KafkaListener(
            topics = "${kafka.topics.domain-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
            throws Exception {
        OutboxMessage message = objectMapper.readValue(record.value(), OutboxMessage.class);
        migrationEventApplyService.applyIfNotExists(message);
        acknowledgment.acknowledge();
    }
}
