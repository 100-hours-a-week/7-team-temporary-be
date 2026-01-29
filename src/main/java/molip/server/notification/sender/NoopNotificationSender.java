package molip.server.notification.sender;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fcm.enabled", havingValue = "false", matchIfMissing = true)
public class NoopNotificationSender implements NotificationSender {

    @Override
    public void send(String title, String content, List<String> tokens) {}
}
