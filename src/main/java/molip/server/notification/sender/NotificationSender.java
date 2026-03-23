package molip.server.notification.sender;

import java.util.List;
import java.util.Map;

public interface NotificationSender {

    void send(String title, String content, Map<String, String> data, List<String> tokens);
}
