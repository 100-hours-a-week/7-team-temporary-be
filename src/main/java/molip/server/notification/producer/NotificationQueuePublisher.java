package molip.server.notification.producer;

public interface NotificationQueuePublisher {

    void publishRequested(Long notificationId);
}
