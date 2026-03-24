package molip.server.notification.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationQueueMessage(
        String eventId, Long notificationId, String eventType, OffsetDateTime occurredAt) {}
