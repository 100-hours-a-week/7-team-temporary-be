package molip.server.notification.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.notification.event.ChatMessageNotificationRequestedEvent;
import molip.server.notification.event.FriendCreatedEvent;
import molip.server.notification.event.FriendRequestedEvent;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.event.PostLikedEvent;
import molip.server.notification.event.ReportCreatedEvent;
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

    @EventListener
    public void handleFriendRequested(FriendRequestedEvent event) {
        notificationCommandFacade.createFriendRequestedNotification(event);
    }

    @EventListener
    public void handleFriendCreated(FriendCreatedEvent event) {
        notificationCommandFacade.createFriendCreatedNotification(event);
    }

    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        notificationCommandFacade.createPostLikedNotification(event);
    }

    @EventListener
    public void handleReportCreated(ReportCreatedEvent event) {
        notificationCommandFacade.createReportCreatedNotification(event);
    }

    @EventListener
    public void handleChatMessageNotificationRequested(
            ChatMessageNotificationRequestedEvent event) {
        notificationCommandFacade.createChatMessageNotification(event);
    }
}
