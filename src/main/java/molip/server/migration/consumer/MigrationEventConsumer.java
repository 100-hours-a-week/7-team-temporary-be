package molip.server.migration.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import molip.server.outbox.core.model.OutboxMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
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
            "migration.consumer.enabled"
        },
        havingValue = "true")
public class MigrationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MigrationEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final MigrationEventApplyService migrationEventApplyService;

    @KafkaListener(
            topics = "${kafka.topics.domain-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
            throws Exception {
        try {
            OutboxMessage message = objectMapper.readValue(record.value(), OutboxMessage.class);
            migrationEventApplyService.applyIfNotExists(message);
            acknowledgment.acknowledge();
        } catch (DataIntegrityViolationException e) {
            log.warn(
                    "migration consumer data integrity violation; will retry. offset={}, key={}",
                    record.offset(),
                    record.key());
            throw e;
        } catch (Exception e) {
            log.warn(
                    "migration consumer error; will retry. offset={}, key={}",
                    record.offset(),
                    record.key(),
                    e);
            throw e;
        }
    }
}
