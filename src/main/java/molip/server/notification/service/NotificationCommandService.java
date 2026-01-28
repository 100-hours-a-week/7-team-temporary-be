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
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private static final String CREATED_TITLE = "일정이 생성되었습니다.";
    private static final String REMINDER_TITLE = "일정이 곧 시작됩니다.";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createScheduleNotifications(NotificationCreatedEvent event) {

        Users user = userRepository.getReferenceById(event.userId());

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
}
