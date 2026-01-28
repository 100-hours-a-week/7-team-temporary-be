package molip.server.notification.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.service.NotificationCommandService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationCreatedEventHandler {

    private final NotificationCommandService notificationCommandService;

    @TransactionalEventListener
    public void handleNotificationCreated(NotificationCreatedEvent event) {

        notificationCommandService.createScheduleNotifications(event);
    }
}
