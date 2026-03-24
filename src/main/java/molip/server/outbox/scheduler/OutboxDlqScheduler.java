package molip.server.outbox.scheduler;

import lombok.RequiredArgsConstructor;
import molip.server.outbox.publisher.OutboxDlqPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.producer.enabled"},
        havingValue = "true")
public class OutboxDlqScheduler {

    private final OutboxDlqPublisher outboxDlqPublisher;

    @Value("${kafka.outbox.dlq.batch-size:200}")
    private int batchSize;

    @Value("${kafka.outbox.retry.max-attempts:5}")
    private int maxRetryAttempts;

    @Scheduled(fixedDelayString = "${kafka.outbox.dlq.fixed-delay-ms:15000}")
    public void publishDlq() {
        outboxDlqPublisher.publishDlq(batchSize, maxRetryAttempts);
    }
}
