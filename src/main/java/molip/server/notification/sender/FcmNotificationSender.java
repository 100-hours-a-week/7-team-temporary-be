package molip.server.notification.sender;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fcm.enabled", havingValue = "true")
public class FcmNotificationSender implements NotificationSender {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void send(String title, String content, List<String> tokens) {

        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        MulticastMessage message =
                MulticastMessage.builder()
                        .addAllTokens(tokens)
                        .putData("title", title)
                        .putData("content", content)
                        .build();

        try {
            log.info(
                    "FCM send requested. tokensCount={}, dataKeys=[title, content], title=\"{}\", content=\"{}\"",
                    tokens.size(),
                    title,
                    content);
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info(
                    "FCM send completed. successCount={}, failureCount={}",
                    response.getSuccessCount(),
                    response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            throw new IllegalStateException("FCM_SEND_FAILED", e);
        }
    }
}
