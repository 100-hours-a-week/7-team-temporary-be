package molip.server.notification.producer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "notification.producer.enabled",
        havingValue = "false",
        matchIfMissing = true)
public class NoopNotificationQueuePublisher implements NotificationQueuePublisher {

    @Override
    public void publishRequested(Long notificationId) {
        // no-op
    }
}
