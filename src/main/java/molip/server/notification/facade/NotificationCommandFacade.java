package molip.server.notification.facade;

import lombok.RequiredArgsConstructor;
import molip.server.notification.event.ChatMessageNotificationRequestedEvent;
import molip.server.notification.event.FriendCreatedEvent;
import molip.server.notification.event.FriendRequestedEvent;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.event.PostLikedEvent;
import molip.server.notification.event.ReportCreatedEvent;
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

    @Transactional
    public void createFriendRequestedNotification(FriendRequestedEvent event) {
        Users user = userService.getUser(event.targetUserId());

        notificationService.createFriendRequestedNotification(user, event.requesterNickname());
    }

    @Transactional
    public void createFriendCreatedNotification(FriendCreatedEvent event) {
        Users user = userService.getUser(event.targetUserId());

        notificationService.createFriendCreatedNotification(user, event.accepterNickname());
    }

    @Transactional
    public void createPostLikedNotification(PostLikedEvent event) {
        Users user = userService.getUser(event.targetUserId());
        notificationService.createPostLikedNotification(user, event.likerNickname());
    }

    @Transactional
    public void createReportCreatedNotification(ReportCreatedEvent event) {
        Users user = userService.getUser(event.targetUserId());
        notificationService.createReportCreatedNotification(user, event.reportId());
    }

    @Transactional
    public void createChatMessageNotification(ChatMessageNotificationRequestedEvent event) {
        Users user = userService.getUser(event.targetUserId());

        notificationService.createChatMessageNotification(
                user, event.roomId(), event.senderNickname(), event.messagePreview());
    }
}
