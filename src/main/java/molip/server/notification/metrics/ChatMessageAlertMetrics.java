package molip.server.notification.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final LongAdder offlineTargetTotal = new LongAdder();
    private final LongAdder notificationRequestedTotal = new LongAdder();
    private final LongAdder notificationCreatedTotal = new LongAdder();
    private final LongAdder dispatchPendingChatTotal = new LongAdder();
    private final LongAdder dispatchAttemptTotal = new LongAdder();
    private final LongAdder dispatchSentTotal = new LongAdder();
    private final LongAdder dispatchFailedTotal = new LongAdder();
    private final LongAdder dispatchNoTokenTotal = new LongAdder();

    private volatile long lastLoggedAtMillis = System.currentTimeMillis();

    public void recordMessageFanout(int offlineTargets, int requestedCount) {
        messageEventTotal.increment();
        if (offlineTargets > 0) {
            offlineTargetTotal.add(offlineTargets);
        }
        if (requestedCount > 0) {
            notificationRequestedTotal.add(requestedCount);
        }

        meterRegistry.counter("chat_alert_message_event_total").increment();
        if (offlineTargets > 0) {
            meterRegistry.counter("chat_alert_offline_target_total").increment(offlineTargets);
        }
        if (requestedCount > 0) {
            meterRegistry
                    .counter("chat_alert_notification_requested_total")
                    .increment(requestedCount);
        }
        logIfNeeded();
    }

    public void recordNotificationCreated() {
        notificationCreatedTotal.increment();
        meterRegistry.counter("chat_alert_notification_created_total").increment();
        logIfNeeded();
    }

    public void recordDispatchPendingChat(int pendingChatCount) {
        if (pendingChatCount <= 0) {
            return;
        }
        dispatchPendingChatTotal.add(pendingChatCount);
        meterRegistry.counter("chat_alert_dispatch_pending_chat_total").increment(pendingChatCount);
        logIfNeeded();
    }

    public void recordDispatchAttempt() {
        dispatchAttemptTotal.increment();
        meterRegistry.counter("chat_alert_dispatch_attempt_total").increment();
        logIfNeeded();
    }

    public void recordDispatchSent(LocalDateTime scheduledAt, LocalDateTime sentAt) {
        dispatchSentTotal.increment();
        meterRegistry.counter("chat_alert_dispatch_sent_total").increment();

        if (scheduledAt != null && sentAt != null) {
            long lagMillis = Math.max(0L, Duration.between(scheduledAt, sentAt).toMillis());
            Timer.builder("chat_alert_dispatch_lag_ms")
                    .description(
                            "Lag from notification scheduledAt to sentAt for chat message alert")
                    .register(meterRegistry)
                    .record(lagMillis, TimeUnit.MILLISECONDS);
        }
        logIfNeeded();
    }

    public void recordDispatchFailed() {
        dispatchFailedTotal.increment();
        meterRegistry.counter("chat_alert_dispatch_failed_total").increment();
        logIfNeeded();
    }

    public void recordDispatchNoToken() {
        dispatchNoTokenTotal.increment();
        meterRegistry.counter("chat_alert_dispatch_no_token_total").increment();
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
            long offline = offlineTargetTotal.sum();
            long requested = notificationRequestedTotal.sum();
            long created = notificationCreatedTotal.sum();
            long pending = dispatchPendingChatTotal.sum();
            long attempt = dispatchAttemptTotal.sum();
            long sent = dispatchSentTotal.sum();
            long failed = dispatchFailedTotal.sum();
            long noToken = dispatchNoTokenTotal.sum();

            double requestPerEvent = event == 0 ? 0.0 : requested * 1.0 / event;
            double sendRate = attempt == 0 ? 0.0 : (sent * 100.0 / attempt);

            log.info(
                    "ALERT_METRIC chat_message event={} offlineTarget={} requested={} created={} pending={} attempt={} sent={} failed={} noToken={} requestPerEvent={} sendRate={}",
                    event,
                    offline,
                    requested,
                    created,
                    pending,
                    attempt,
                    sent,
                    failed,
                    noToken,
                    String.format("%.2f", requestPerEvent),
                    String.format("%.2f", sendRate));

            lastLoggedAtMillis = now;
        }
    }
}
