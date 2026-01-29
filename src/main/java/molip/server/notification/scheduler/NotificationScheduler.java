package molip.server.notification.scheduler;

import lombok.RequiredArgsConstructor;
import molip.server.notification.facade.NotificationDispatchFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationDispatchFacade notificationDispatchFacade;

    @Value("${notification.dispatch.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${notification.dispatch.fixed-delay-ms:60000}")
    public void dispatchPendingNotifications() {

        notificationDispatchFacade.dispatchPendingNotifications(batchSize);
    }
}
