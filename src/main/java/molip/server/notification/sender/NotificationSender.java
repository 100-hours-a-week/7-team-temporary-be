package molip.server.notification.sender;

import java.util.List;

public interface NotificationSender {

    void send(String title, String content, List<String> tokens);
}
