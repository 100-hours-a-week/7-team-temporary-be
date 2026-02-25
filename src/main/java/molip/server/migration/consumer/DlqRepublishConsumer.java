package molip.server.migration.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.consumer.enabled", "kafka.consumer.dlq.republish-enabled"},
        havingValue = "true")
public class DlqRepublishConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqRepublishConsumer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.domain-events}")
    private String domainEventsTopic;

    @Value("${kafka.consumer.dlq.republish-delay-ms:0}")
    private long republishDelayMs;

    @KafkaListener(
            topics = "${kafka.topics.domain-events}.DLQ",
            groupId = "${spring.kafka.consumer.group-id}.dlq")
    public void republish(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
            throws Exception {
        if (republishDelayMs > 0) {
            Thread.sleep(republishDelayMs);
        }
        try {
            kafkaTemplate.send(domainEventsTopic, record.key(), record.value()).get();
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.warn(
                    "DLQ republish failed; will retry. topic={}, key={}, offset={}",
                    record.topic(),
                    record.key(),
                    record.offset(),
                    e);
            throw e;
        }
    }
}
