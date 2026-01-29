package molip.server.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.NotificationStatus;
import molip.server.common.enums.NotificationType;
import molip.server.notification.entity.Notification;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.repository.NotificationRepository;
import molip.server.user.entity.Users;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private static final String CREATED_TITLE = "일정이 생성되었습니다.";
    private static final String REMINDER_TITLE = "일정이 곧 시작됩니다.";

    @Transactional
    public List<Notification> getPendingNotifications(LocalDateTime now, int batchSize) {

        return notificationRepository.findPendingNotifications(
                NotificationStatus.PENDING, now, PageRequest.of(0, batchSize));
    }

    @Transactional
    public void createScheduleNotifications(Users user, NotificationCreatedEvent event) {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications = new ArrayList<>();

        notifications.add(
                new Notification(
                        user,
                        NotificationType.SCHEDULE_CREATED,
                        CREATED_TITLE,
                        event.title(),
                        NotificationStatus.PENDING,
                        now));

        if (event.startAt() != null && event.planDate() != null) {

            LocalDateTime scheduledAt =
                    LocalDateTime.of(event.planDate(), event.startAt()).minusMinutes(5);

            notifications.add(
                    new Notification(
                            user,
                            NotificationType.SCHEDULE_REMINDER,
                            REMINDER_TITLE,
                            event.title(),
                            NotificationStatus.PENDING,
                            scheduledAt));
        }

        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void markSent(Notification notification, LocalDateTime sentAt) {

        notification.markSent(sentAt);
    }

    @Transactional
    public void markFailed(Notification notification) {

        notification.markFailed();
    }
}
