package molip.server.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.NotificationStatus;
import molip.server.common.enums.NotificationTitle;
import molip.server.common.enums.NotificationType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.notification.entity.Notification;
import molip.server.notification.event.NotificationCreatedEvent;
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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    @Transactional
    public List<Notification> getPendingNotifications(LocalDateTime now, int batchSize) {

        return notificationRepository.findPendingNotifications(
                NotificationStatus.PENDING, now, PageRequest.of(0, batchSize));
    }

    @Transactional(readOnly = true)
    public Page<Notification> getSentNotifications(Long userId, int page, int size) {

        validatePage(page, size);

        return notificationRepository.findSentNotifications(
                userId, NotificationStatus.SENT, PageRequest.of(page - 1, size));
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
            notificationRepository.saveAll(notifications);
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

        notificationRepository.save(
                new Notification(
                        user,
                        scheduleId,
                        NotificationType.SCHEDULE_REMINDER,
                        NotificationTitle.SCHEDULE_REMINDER.getValue(),
                        buildReminderContent(title, startAt),
                        NotificationStatus.PENDING,
                        scheduledAt));
    }

    @Transactional
    public void markSent(Notification notification, LocalDateTime sentAt) {

        notification.markSent(sentAt);
    }

    @Transactional
    public void markFailed(Notification notification) {

        notification.markFailed();
    }

    @Transactional
    public void deleteScheduleReminders(Long scheduleId) {

        notificationRepository
                .findByScheduleIdAndTypeAndDeletedAtIsNull(
                        scheduleId, NotificationType.SCHEDULE_REMINDER)
                .forEach(Notification::deleteNotification);
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
}
