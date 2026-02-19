package molip.server.migration.outbox;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
public class OutboxRetryScheduler {

    private final OutboxRepository outboxRepository;

    @Value("${kafka.outbox.retry.max-attempts:5}")
    private int maxRetryAttempts;

    @Value("${kafka.outbox.retry.delay-ms:60000}")
    private long retryDelayMs;

    @Value("${kafka.outbox.retry.batch-size:200}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${kafka.outbox.retry.fixed-delay-ms:10000}")
    public void requeueFailed() {
        OffsetDateTime retryBefore =
                OffsetDateTime.now(ZoneOffset.UTC).minusNanos(retryDelayMs * 1_000_000L);
        List<OutboxRecord> retryable =
                outboxRepository.findRetryableFailed(
                        batchSize, maxRetryAttempts, retryBefore);
        if (retryable.isEmpty()) {
            return;
        }
        List<Long> ids = retryable.stream().map(OutboxRecord::id).toList();
        outboxRepository.markPending(ids);
    }
}
