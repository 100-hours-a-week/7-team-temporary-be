package molip.server.migration.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.producer.enabled"},
        havingValue = "true")
public class OutboxScheduler {

    private final OutboxPublisher outboxPublisher;

    @Value("${kafka.outbox.batch-size:200}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${kafka.outbox.fixed-delay-ms:5000}")
    public void publishOutboxEvents() {
        outboxPublisher.publishPending(batchSize);
    }
}
