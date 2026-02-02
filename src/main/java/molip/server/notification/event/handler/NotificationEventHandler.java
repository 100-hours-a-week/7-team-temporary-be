package molip.server.notification.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.event.ScheduleReminderResetEvent;
import molip.server.notification.facade.NotificationCommandFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationCommandFacade notificationCommandFacade;

    @EventListener
    public void handleNotificationCreated(NotificationCreatedEvent event) {

        notificationCommandFacade.createScheduleNotifications(event);
    }

    @EventListener
    public void handleScheduleReminderReset(ScheduleReminderResetEvent event) {

        notificationCommandFacade.resetScheduleReminder(event);
    }
}
