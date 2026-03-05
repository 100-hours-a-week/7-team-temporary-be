package molip.server.socket.service;

import lombok.RequiredArgsConstructor;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketUserChannelBroadcaster {

    private static final String USER_QUEUE_DESTINATION = "/queue/user";
    private static final String UNREAD_CHANGED_EVENT = "unreadChanged";

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendUnreadChanged(String sessionId, SocketUnreadChangedResponse payload) {
        sendToSession(sessionId, UNREAD_CHANGED_EVENT, payload);
    }

    public void sendToSession(String sessionId, String event, Object payload) {
        SimpMessageHeaderAccessor headerAccessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(
                sessionId,
                USER_QUEUE_DESTINATION,
                SocketEventResponse.of(event, payload),
                headerAccessor.getMessageHeaders());
    }
}
