package molip.server.socket.listener;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.socket.dto.response.SocketConnectedResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Component
@RequiredArgsConstructor
public class SocketStompSessionListener {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SOCKET_CONNECTED_EVENT = "socket.connected";

    private final SimpMessagingTemplate simpMessagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        Principal user = accessor.getUser();
        if (sessionAttributes == null || user == null) {
            return;
        }

        Object userIdAttribute = sessionAttributes.get("userId");
        Object connectedAtAttribute = sessionAttributes.get("connectedAt");
        if (!(userIdAttribute instanceof Long userId) || !(connectedAtAttribute instanceof String connectedAt)) {
            return;
        }

        simpMessagingTemplate.convertAndSendToUser(
                user.getName(),
                "/queue/handshake",
                SocketEventResponse.of(
                        SOCKET_CONNECTED_EVENT,
                        SocketConnectedResponse.of(
                                accessor.getSessionId(),
                                userId,
                                OffsetDateTime.parse(connectedAt),
                                OffsetDateTime.now(KOREA_ZONE_ID))));
    }
}
