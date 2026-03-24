package molip.server.notification.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.notification.consumer.NotificationQueueMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.producer.enabled", "notification.producer.enabled"},
        havingValue = "true")
public class KafkaNotificationQueuePublisher implements NotificationQueuePublisher {

    private static final String EVENT_TYPE = "NOTIFICATION_REQUESTED";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.kafka.topics.requested}")
    private String requestedTopic;

    @Override
    public void publishRequested(Long notificationId) {
        if (notificationId == null) {
            return;
        }
        try {
            NotificationQueueMessage message =
                    new NotificationQueueMessage(
                            UUID.randomUUID().toString(),
                            notificationId,
                            EVENT_TYPE,
                            OffsetDateTime.now(ZoneOffset.UTC));
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(requestedTopic, String.valueOf(notificationId), payload).get();
        } catch (Exception e) {
            log.warn(
                    "notification queue publish failed. topic={}, notificationId={}, reason={}",
                    requestedTopic,
                    notificationId,
                    e.getMessage());
        }
    }
}
