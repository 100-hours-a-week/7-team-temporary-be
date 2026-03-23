package molip.server.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageAlertMetrics {

    private static final long LOG_INTERVAL_MS = 60_000L;

    private final MeterRegistry meterRegistry;

    private final LongAdder messageEventTotal = new LongAdder();
    private final LongAdder notificationCreatedTotal = new LongAdder();
    private final LongAdder dispatchAttemptTotal = new LongAdder();
    private final LongAdder dispatchSentTotal = new LongAdder();
    private final LongAdder dispatchLagTotalMs = new LongAdder();
    private final LongAdder dispatchLagCount = new LongAdder();

    private volatile long lastLoggedAtMillis = System.currentTimeMillis();
    private Counter messageEventCounter;
    private Counter notificationCreatedCounter;
    private Counter dispatchAttemptCounter;
    private Counter dispatchSentCounter;
    private Timer dispatchLagTimer;

    @PostConstruct
    void initMeters() {
        messageEventCounter = meterRegistry.counter("chat_alert_message_event_total");
        notificationCreatedCounter = meterRegistry.counter("chat_alert_notification_created_total");
        dispatchAttemptCounter = meterRegistry.counter("chat_alert_dispatch_attempt_total");
        dispatchSentCounter = meterRegistry.counter("chat_alert_dispatch_sent_total");
        dispatchLagTimer =
                Timer.builder("chat_alert_dispatch_lag_ms")
                        .description(
                                "Lag from notification scheduledAt to sentAt for chat message alert")
                        .register(meterRegistry);
    }

    public void recordMessageEvent() {
        messageEventTotal.increment();
        messageEventCounter.increment();
        logIfNeeded();
    }

    public void recordNotificationCreated() {
        notificationCreatedTotal.increment();
        notificationCreatedCounter.increment();
        logIfNeeded();
    }

    public void recordDispatchAttempt() {
        dispatchAttemptTotal.increment();
        dispatchAttemptCounter.increment();
        logIfNeeded();
    }

    public void recordDispatchSent(LocalDateTime scheduledAt, LocalDateTime sentAt) {
        dispatchSentTotal.increment();
        dispatchSentCounter.increment();

        if (scheduledAt != null && sentAt != null) {
            long lagMillis = Math.max(0L, Duration.between(scheduledAt, sentAt).toMillis());
            dispatchLagTotalMs.add(lagMillis);
            dispatchLagCount.increment();
            dispatchLagTimer.record(lagMillis, TimeUnit.MILLISECONDS);
        }
        logIfNeeded();
    }

    private void logIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastLoggedAtMillis < LOG_INTERVAL_MS) {
            return;
        }
        synchronized (this) {
            if (now - lastLoggedAtMillis < LOG_INTERVAL_MS) {
                return;
            }

            long event = messageEventTotal.sum();
            long created = notificationCreatedTotal.sum();
            long attempt = dispatchAttemptTotal.sum();
            long sent = dispatchSentTotal.sum();
            long lagTotalMs = dispatchLagTotalMs.sum();
            long lagCount = dispatchLagCount.sum();

            double sendRate = attempt == 0 ? 0.0 : (sent * 100.0 / attempt);
            double avgLagMs = lagCount == 0 ? 0.0 : (lagTotalMs * 1.0 / lagCount);

            log.info(
                    "ALERT_METRIC chat_message event={} created={} attempt={} sent={} sendRate={} lagAvgMs={}",
                    event,
                    created,
                    attempt,
                    sent,
                    String.format("%.2f", sendRate),
                    String.format("%.2f", avgLagMs));

            lastLoggedAtMillis = now;
        }
    }
}
