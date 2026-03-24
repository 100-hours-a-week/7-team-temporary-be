package molip.server.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.notification.facade.NotificationDispatchFacade;
import molip.server.notification.metrics.NotificationQueueMetrics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = {"kafka.enabled", "kafka.consumer.enabled", "notification.consumer.enabled"},
        havingValue = "true")
public class NotificationQueueConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationDispatchFacade notificationDispatchFacade;
    private final NotificationConsumeIdempotencyService idempotencyService;
    private final NotificationQueueMetrics notificationQueueMetrics;

    @KafkaListener(
            topics = "${notification.kafka.topics.requested}",
            groupId =
                    "${notification.kafka.consumer.group-id:${spring.kafka.consumer.group-id}.notification}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
            throws Exception {
        notificationQueueMetrics.recordConsume();
        try {
            NotificationQueueMessage message =
                    objectMapper.readValue(record.value(), NotificationQueueMessage.class);

            if (message.notificationId() == null) {
                notificationQueueMetrics.recordSkip();
                acknowledgment.acknowledge();
                return;
            }

            if (!idempotencyService.markIfFirst(message.eventId())) {
                notificationQueueMetrics.recordDuplicate();
                acknowledgment.acknowledge();
                return;
            }

            boolean dispatched =
                    notificationDispatchFacade.dispatchNotificationById(message.notificationId());
            if (dispatched) {
                notificationQueueMetrics.recordSuccess();
            } else {
                notificationQueueMetrics.recordSkip();
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            notificationQueueMetrics.recordFail();
            log.warn(
                    "notification consumer error. topic={}, offset={}, key={}",
                    record.topic(),
                    record.offset(),
                    record.key(),
                    e);
            throw e;
        }
    }
}
