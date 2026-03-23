package molip.server.notification.facade;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.common.enums.NotificationType;
import molip.server.notification.entity.Notification;
import molip.server.notification.metrics.ChatMessageAlertMetrics;
import molip.server.notification.sender.NotificationSender;
import molip.server.notification.service.NotificationService;
import molip.server.notification.service.UserFcmTokenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchFacade {

    private final NotificationService notificationService;
    private final UserFcmTokenService userFcmTokenService;
    private final NotificationSender notificationSender;
    private final ChatMessageAlertMetrics chatMessageAlertMetrics;

    @Transactional
    public void dispatchPendingNotifications(int batchSize) {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications =
                notificationService.getPendingNotifications(now, batchSize);
        int pendingChatCount =
                (int)
                        notifications.stream()
                                .filter(
                                        notification ->
                                                notification.getType()
                                                        == NotificationType.CHAT_MESSAGE)
                                .count();
        chatMessageAlertMetrics.recordDispatchPendingChat(pendingChatCount);

        for (Notification notification : notifications) {

            Long userId = notification.getUser().getId();
            boolean isChatMessage = notification.getType() == NotificationType.CHAT_MESSAGE;
            if (isChatMessage) {
                chatMessageAlertMetrics.recordDispatchAttempt();
            }

            List<String> tokens = userFcmTokenService.getActiveTokens(userId);

            if (tokens.isEmpty()) {
                if (isChatMessage) {
                    chatMessageAlertMetrics.recordDispatchNoToken();
                }

                notificationService.markFailed(notification);
                continue;
            }

            try {

                notificationSender.send(notification.getTitle(), notification.getContent(), tokens);

                notificationService.markSent(notification, now);
                if (isChatMessage) {
                    chatMessageAlertMetrics.recordDispatchSent(notification.getScheduledAt(), now);
                }
            } catch (Exception e) {
                log.warn(
                        "notification dispatch failed: notificationId={}, type={}, userId={}, reason={}",
                        notification.getId(),
                        notification.getType(),
                        userId,
                        e.getMessage());
                if (isChatMessage) {
                    chatMessageAlertMetrics.recordDispatchFailed();
                }

                notificationService.markFailed(notification);
            }
        }
    }
}
