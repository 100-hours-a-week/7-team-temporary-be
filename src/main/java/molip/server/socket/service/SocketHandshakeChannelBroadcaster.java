package molip.server.socket.service;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import molip.server.socket.dto.response.SocketDisconnectResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketPingResponse;
import molip.server.socket.dto.response.SocketReconnectRequiredResponse;
import molip.server.socket.dto.response.SocketResyncRequiredResponse;
import molip.server.socket.dto.response.SocketSessionReleasedResponse;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketHandshakeChannelBroadcaster {

    private static final String HANDSHAKE_QUEUE_DESTINATION = "/queue/handshake";
    private static final String SOCKET_DISCONNECT_EVENT = "socket.disconnect";
    private static final String SOCKET_RECONNECT_REQUIRED_EVENT = "socket.reconnectRequired";
    private static final String SOCKET_RESYNC_REQUIRED_EVENT = "socket.resyncRequired";
    private static final String SOCKET_PING_EVENT = "socket.ping";
    private static final String SOCKET_SESSION_RELEASED_EVENT = "socket.sessionReleased";

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendDisconnect(String sessionId, String code, String message) {
        sendToSession(
                sessionId, SOCKET_DISCONNECT_EVENT, SocketDisconnectResponse.of(code, message));
    }

    public void sendReconnectRequired(
            String sessionId, String code, String message, Long retryAfterMs) {
        sendToSession(
                sessionId,
                SOCKET_RECONNECT_REQUIRED_EVENT,
                SocketReconnectRequiredResponse.of(code, message, retryAfterMs));
    }

    public void sendResyncRequired(
            String sessionId, String scope, Long roomId, Long fromMessageId) {
        sendToSession(
                sessionId,
                SOCKET_RESYNC_REQUIRED_EVENT,
                SocketResyncRequiredResponse.of(scope, roomId, fromMessageId));
    }

    public void sendPing(String sessionId, OffsetDateTime timestamp) {
        sendToSession(sessionId, SOCKET_PING_EVENT, SocketPingResponse.of(timestamp));
    }

    public void sendSessionReleased(Long userId, String sessionId, boolean canReconnect) {
        sendToSession(
                sessionId,
                SOCKET_SESSION_RELEASED_EVENT,
                SocketSessionReleasedResponse.of(userId, sessionId, canReconnect));
    }

    private void sendToSession(String sessionId, String event, Object payload) {
        SimpMessageHeaderAccessor headerAccessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(
                sessionId,
                HANDSHAKE_QUEUE_DESTINATION,
                SocketEventResponse.of(event, payload),
                headerAccessor.getMessageHeaders());
    }
}
