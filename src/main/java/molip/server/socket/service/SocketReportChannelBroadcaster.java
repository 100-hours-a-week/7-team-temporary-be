package molip.server.socket.service;

import lombok.RequiredArgsConstructor;
import molip.server.socket.dto.response.SocketEventResponse;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketReportChannelBroadcaster {

    private static final String REPORT_QUEUE_DESTINATION = "/queue/report";

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendToSession(String sessionId, String event, Object payload) {
        SimpMessageHeaderAccessor headerAccessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(
                sessionId,
                REPORT_QUEUE_DESTINATION,
                SocketEventResponse.of(event, payload),
                headerAccessor.getMessageHeaders());
    }
}
