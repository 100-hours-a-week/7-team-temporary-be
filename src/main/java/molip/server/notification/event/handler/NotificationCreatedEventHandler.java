package molip.server.notification.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.facade.NotificationCommandFacade;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationCreatedEventHandler {

    private final NotificationCommandFacade notificationCommandFacade;

    @TransactionalEventListener
    public void handleNotificationCreated(NotificationCreatedEvent event) {

        notificationCommandFacade.createScheduleNotifications(event);
    }
}
