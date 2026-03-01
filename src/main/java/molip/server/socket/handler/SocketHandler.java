package molip.server.socket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketEventRequest;
import molip.server.socket.dto.response.SocketConnectedResponse;
import molip.server.socket.dto.response.SocketErrorResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {

    private static final String SOCKET_CONNECT_EVENT = "socket.connect";
    private static final String SOCKET_CONNECTED_EVENT = "socket.connected";
    private static final String SOCKET_ERROR_EVENT = "socket.error";

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final RedisSocketSessionStore socketSessionStore;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        SocketEventRequest eventRequest;
        try {
            eventRequest = objectMapper.readValue(message.getPayload(), SocketEventRequest.class);
        } catch (IOException e) {
            sendErrorAndClose(
                    session,
                    "CONNECT_INVALID_PAYLOAD",
                    "소켓 연결 요청 형식이 올바르지 않습니다.",
                    CloseStatus.BAD_DATA);
            return;
        }

        if (!SOCKET_CONNECT_EVENT.equals(eventRequest.event()) || eventRequest.payload() == null) {
            return;
        }

        SocketConnectRequest connectRequest =
                objectMapper.treeToValue(eventRequest.payload(), SocketConnectRequest.class);

        handleConnect(session, connectRequest);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        super.afterConnectionClosed(session, status);
    }

    private void handleConnect(WebSocketSession session, SocketConnectRequest connectRequest)
            throws IOException {
        String bearerToken = connectRequest.accessToken();
        String deviceId = connectRequest.deviceId();

        if (bearerToken == null || bearerToken.isBlank() || deviceId == null || deviceId.isBlank()) {
            sendErrorAndClose(
                    session,
                    "CONNECT_INVALID_PAYLOAD",
                    "accessToken 또는 deviceId가 누락되었습니다.",
                    CloseStatus.BAD_DATA);
            return;
        }

        String token = stripBearerPrefix(bearerToken);
        if (token == null) {
            sendErrorAndClose(
                    session,
                    "CONNECT_INVALID_PAYLOAD",
                    "accessToken 형식이 올바르지 않습니다.",
                    CloseStatus.BAD_DATA);
            return;
        }

        JwtValidationStatus tokenStatus = jwtTokenProvider.getAccessTokenStatus(token);
        if (tokenStatus == JwtValidationStatus.EXPIRED) {
            sendErrorAndClose(
                    session,
                    "CONNECT_TOKEN_EXPIRED",
                    "액세스 토큰이 만료되었습니다.",
                    CloseStatus.POLICY_VIOLATION);
            return;
        }

        if (tokenStatus == JwtValidationStatus.INVALID) {
            sendErrorAndClose(
                    session,
                    "CONNECT_UNAUTHORIZED",
                    "유효하지 않은 토큰입니다.",
                    CloseStatus.POLICY_VIOLATION);
            return;
        }

        Long userId = jwtUtil.extractUserId(token);
        String tokenDeviceId = jwtUtil.extractDeviceId(token);
        if (userId == null) {
            sendErrorAndClose(
                    session,
                    "CONNECT_UNAUTHORIZED",
                    "유효하지 않은 토큰입니다.",
                    CloseStatus.POLICY_VIOLATION);
            return;
        }

        if (tokenDeviceId == null || !tokenDeviceId.equals(deviceId)) {
            sendErrorAndClose(
                    session,
                    "CONNECT_INVALID_PAYLOAD",
                    "deviceId가 토큰 정보와 일치하지 않습니다.",
                    CloseStatus.BAD_DATA);
            return;
        }

        OffsetDateTime connectedAt = OffsetDateTime.now(ZoneOffset.UTC);

        session.getAttributes().put("accessToken", bearerToken);
        session.getAttributes().put("deviceId", deviceId);
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("connectedAt", connectedAt.toString());

        socketSessionStore.save(session.getId(), userId, deviceId, connectedAt);

        sendEvent(
                session,
                SOCKET_CONNECTED_EVENT,
                SocketConnectedResponse.of(
                        session.getId(), userId, connectedAt, OffsetDateTime.now(ZoneOffset.UTC)));
    }

    private void sendErrorAndClose(
            WebSocketSession session, String code, String message, CloseStatus closeStatus)
            throws IOException {
        sendEvent(session, SOCKET_ERROR_EVENT, SocketErrorResponse.of(code, message));
        session.close(closeStatus);
    }

    private void sendEvent(WebSocketSession session, String event, Object payload)
            throws IOException {
        String response =
                objectMapper.writeValueAsString(SocketEventResponse.of(event, payload));
        session.sendMessage(new TextMessage(response));
    }

    private String stripBearerPrefix(String bearerToken) {
        if (!bearerToken.startsWith("Bearer ")) {
            return null;
        }

        String token = bearerToken.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}
