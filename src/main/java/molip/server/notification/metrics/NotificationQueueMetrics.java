package molip.server.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationQueueMetrics {

    private final MeterRegistry meterRegistry;

    private Counter consumeTotal;
    private Counter consumeSuccessTotal;
    private Counter consumeSkipTotal;
    private Counter consumeDuplicateTotal;
    private Counter consumeFailTotal;

    @PostConstruct
    void init() {
        consumeTotal = meterRegistry.counter("notification_consume_total");
        consumeSuccessTotal = meterRegistry.counter("notification_consume_success_total");
        consumeSkipTotal = meterRegistry.counter("notification_consume_skip_total");
        consumeDuplicateTotal = meterRegistry.counter("notification_consume_duplicate_total");
        consumeFailTotal = meterRegistry.counter("notification_consume_fail_total");
    }

    public void recordConsume() {
        consumeTotal.increment();
    }

    public void recordSuccess() {
        consumeSuccessTotal.increment();
    }

    public void recordSkip() {
        consumeSkipTotal.increment();
    }

    public void recordDuplicate() {
        consumeDuplicateTotal.increment();
    }

    public void recordFail() {
        consumeFailTotal.increment();
    }
}
