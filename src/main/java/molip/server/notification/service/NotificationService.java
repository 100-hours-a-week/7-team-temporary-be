package molip.server.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.NotificationStatus;
import molip.server.common.enums.NotificationTitle;
import molip.server.common.enums.NotificationType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.notification.entity.Notification;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.metrics.ChatMessageAlertMetrics;
import molip.server.notification.repository.NotificationRepository;
import molip.server.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final OutboxEventService outboxEventService;
    private final ChatMessageAlertMetrics chatMessageAlertMetrics;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    @Transactional
    public List<Notification> getPendingNotifications(LocalDateTime now, int batchSize) {

        return notificationRepository.findPendingNotifications(
                NotificationStatus.PENDING, now, PageRequest.of(0, batchSize));
    }

    @Transactional(readOnly = true)
    public Optional<Notification> getDispatchableNotification(Long notificationId) {
        return notificationRepository.findByIdAndDeletedAtIsNull(notificationId);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getSentNotifications(Long userId, int page, int size) {

        validatePage(page, size);

        return notificationRepository.findSentNotifications(
                userId,
                NotificationStatus.SENT,
                NotificationType.CHAT_MESSAGE,
                PageRequest.of(page - 1, size));
    }

    @Transactional
    public void createScheduleNotifications(Users user, NotificationCreatedEvent event) {

        List<Notification> notifications = new ArrayList<>();

        if (event.startAt() != null && event.planDate() != null) {

            LocalDateTime scheduledAt =
                    LocalDateTime.of(event.planDate(), event.startAt()).minusMinutes(5);

            if (shouldCreateReminder(scheduledAt)) {
                notifications.add(
                        new Notification(
                                user,
                                event.scheduleId(),
                                NotificationType.SCHEDULE_REMINDER,
                                NotificationTitle.SCHEDULE_REMINDER.getValue(),
                                buildReminderContent(event.title(), event.startAt()),
                                NotificationStatus.PENDING,
                                scheduledAt));
            }
        }

        if (!notifications.isEmpty()) {
            List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
            for (Notification notification : savedNotifications) {
                outboxEventService.recordCreated(
                        AggregateType.NOTIFICATION,
                        notification.getId(),
                        OutboxPayloadMapper.notification(notification));
            }
        }
    }

    @Transactional
    public void resetScheduleReminder(
            Users user, Long scheduleId, String title, LocalDate planDate, LocalTime startAt) {

        deleteScheduleReminders(scheduleId);

        if (startAt == null || planDate == null) {
            return;
        }

        LocalDateTime scheduledAt = LocalDateTime.of(planDate, startAt).minusMinutes(5);

        if (!shouldCreateReminder(scheduledAt)) {
            return;
        }

        Notification notification =
                notificationRepository.save(
                        new Notification(
                                user,
                                scheduleId,
                                NotificationType.SCHEDULE_REMINDER,
                                NotificationTitle.SCHEDULE_REMINDER.getValue(),
                                buildReminderContent(title, startAt),
                                NotificationStatus.PENDING,
                                scheduledAt));
        outboxEventService.recordCreated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(notification));
    }

    @Transactional
    public void createFriendRequestedNotification(Users user, String requesterNickname) {
        Notification notification =
                notificationRepository.save(
                        new Notification(
                                user,
                                null,
                                NotificationType.FRIEND_REQUESTED,
                                buildFriendRequestedTitle(requesterNickname),
                                "목록에서 확인해주세요!",
                                NotificationStatus.PENDING,
                                LocalDateTime.now()));

        outboxEventService.recordCreated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(notification));
    }

    @Transactional
    public void createFriendCreatedNotification(Users user, String accepterNickname) {
        Notification notification =
                notificationRepository.save(
                        new Notification(
                                user,
                                null,
                                NotificationType.FRIEND_CREATED,
                                buildFriendCreatedTitle(accepterNickname),
                                "친구 요청이 수락되어 친구 관계가 맺어졌습니다.",
                                NotificationStatus.PENDING,
                                LocalDateTime.now()));

        outboxEventService.recordCreated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(notification));
    }

    @Transactional
    public void createPostLikedNotification(Users user, Long reflectionId, String likerNickname) {
        Notification notification =
                notificationRepository.save(
                        new Notification(
                                user,
                                reflectionId,
                                NotificationType.POST_LIKED,
                                buildPostLikedTitle(likerNickname),
                                "회고를 확인해보세요.",
                                NotificationStatus.PENDING,
                                LocalDateTime.now()));

        outboxEventService.recordCreated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(
                        notification, Map.of("reflection_id", reflectionId)));
    }

    @Transactional
    public void createReportCreatedNotification(Users user, Long reportId) {
        Notification notification =
                notificationRepository.save(
                        new Notification(
                                user,
                                reportId,
                                NotificationType.REPORT_CREATED,
                                NotificationTitle.REPORT_CREATED.getValue(),
                                "새로운 리포트를 확인해보세요.",
                                NotificationStatus.PENDING,
                                LocalDateTime.now()));

        outboxEventService.recordCreated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(notification));
    }

    @Transactional
    public void createChatMessageNotification(
            Users user,
            Long roomId,
            Long messageId,
            Integer unreadCount,
            Long senderUserId,
            String senderNickname,
            String messagePreview) {
        String content = buildChatMessageContent(messagePreview);
        Notification notification =
                notificationRepository.save(
                        new Notification(
                                user,
                                roomId,
                                NotificationType.CHAT_MESSAGE,
                                buildChatMessageTitle(senderNickname),
                                content,
                                NotificationStatus.PENDING,
                                LocalDateTime.now()));

        outboxEventService.recordCreated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(
                        notification,
                        Map.of(
                                "room_id", roomId,
                                "message_id", messageId,
                                "unread_count", unreadCount,
                                "sender_user_id", senderUserId,
                                "sender_nickname", senderNickname)));
        chatMessageAlertMetrics.recordNotificationCreated();
    }

    @Transactional
    public void markSent(Notification notification, LocalDateTime sentAt) {

        notification.markSent(sentAt);
        outboxEventService.recordUpdated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(notification));
    }

    @Transactional
    public void markFailed(Notification notification) {

        notification.markFailed();
        outboxEventService.recordUpdated(
                AggregateType.NOTIFICATION,
                notification.getId(),
                OutboxPayloadMapper.notification(notification));
    }

    @Transactional
    public void deleteScheduleReminders(Long scheduleId) {

        List<Notification> notifications =
                notificationRepository
                        .findByScheduleIdAndTypeAndDeletedAtIsNull(
                                scheduleId, NotificationType.SCHEDULE_REMINDER)
                        .stream()
                        .toList();
        for (Notification notification : notifications) {
            notification.deleteNotification();
            outboxEventService.recordDeleted(
                    AggregateType.NOTIFICATION,
                    notification.getId(),
                    OutboxPayloadMapper.notification(notification));
        }
    }

    private void validatePage(int page, int size) {

        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }

    private String buildReminderContent(String title, LocalTime startAt) {

        String time = startAt == null ? "" : startAt.format(TIME_FORMATTER);
        if (time.isBlank()) {
            return title;
        }
        return time + " " + title;
    }

    private boolean shouldCreateReminder(LocalDateTime scheduledAt) {

        return !scheduledAt.isBefore(LocalDateTime.now());
    }

    private String buildFriendRequestedTitle(String requesterNickname) {
        return requesterNickname + "님이 친구 요청을 보냈습니다.";
    }

    private String buildFriendCreatedTitle(String accepterNickname) {
        return accepterNickname + "님이 친구 요청을 수락했습니다.";
    }

    private String buildPostLikedTitle(String likerNickname) {
        return likerNickname + "님이 회고에 좋아요를 눌렀습니다.";
    }

    private String buildChatMessageTitle(String senderNickname) {
        return NotificationTitle.CHAT_MESSAGE.getValue();
    }

    private String buildChatMessageContent(String messagePreview) {
        if (messagePreview == null || messagePreview.isBlank()) {
            return NotificationTitle.CHAT_MESSAGE.getValue();
        }
        return messagePreview;
    }
}
