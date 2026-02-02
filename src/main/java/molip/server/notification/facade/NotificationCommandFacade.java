package molip.server.notification.facade;

import lombok.RequiredArgsConstructor;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.event.ScheduleReminderResetEvent;
import molip.server.notification.service.NotificationService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationCommandFacade {

    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public void createScheduleNotifications(NotificationCreatedEvent event) {

        Users user = userService.getUser(event.userId());

        notificationService.createScheduleNotifications(user, event);
    }

    @Transactional
    public void resetScheduleReminder(ScheduleReminderResetEvent event) {

        Users user = userService.getUser(event.userId());

        notificationService.resetScheduleReminder(
                user, event.scheduleId(), event.title(), event.planDate(), event.startAt());
    }
}
