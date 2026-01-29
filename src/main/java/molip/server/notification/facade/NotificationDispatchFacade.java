package molip.server.notification.facade;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.notification.entity.Notification;
import molip.server.notification.sender.NotificationSender;
import molip.server.notification.service.NotificationService;
import molip.server.notification.service.UserFcmTokenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationDispatchFacade {

    private final NotificationService notificationService;
    private final UserFcmTokenService userFcmTokenService;
    private final NotificationSender notificationSender;

    @Transactional
    public void dispatchPendingNotifications(int batchSize) {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications =
                notificationService.getPendingNotifications(now, batchSize);

        for (Notification notification : notifications) {

            Long userId = notification.getUser().getId();

            List<String> tokens = userFcmTokenService.getActiveTokens(userId);

            if (tokens.isEmpty()) {

                notificationService.markFailed(notification);
                continue;
            }

            try {

                notificationSender.send(notification.getTitle(), notification.getContent(), tokens);

                notificationService.markSent(notification, now);
            } catch (Exception e) {

                notificationService.markFailed(notification);
            }
        }
    }
}
