package molip.server.socket.controller;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketDisconnectRequest;
import molip.server.socket.dto.response.SocketConnectedResponse;
import molip.server.socket.dto.response.SocketErrorResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SocketStompController {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SOCKET_CONNECTED_EVENT = "socket.connected";
    private static final String SOCKET_ERROR_EVENT = "socket.error";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final RedisSocketSessionStore socketSessionStore;

    @MessageMapping("/handshake/connect")
    @SendToUser(value = "/queue/handshake", broadcast = false)
    public SocketEventResponse<?> connect(
            SocketConnectRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        if (request == null
                || isBlank(request.accessToken())
                || isBlank(request.deviceId())) {
            return error("CONNECT_INVALID_PAYLOAD", "accessToken 또는 deviceId가 누락되었습니다.", false);
        }

        String token = stripBearerPrefix(request.accessToken());
        if (token == null) {
            return error("CONNECT_INVALID_PAYLOAD", "accessToken 형식이 올바르지 않습니다.", false);
        }

        JwtValidationStatus tokenStatus = jwtTokenProvider.getAccessTokenStatus(token);
        if (tokenStatus == JwtValidationStatus.EXPIRED) {
            return error("CONNECT_TOKEN_EXPIRED", "액세스 토큰이 만료되었습니다.", true);
        }

        if (tokenStatus == JwtValidationStatus.INVALID) {
            return error("CONNECT_UNAUTHORIZED", "유효하지 않은 토큰입니다.", false);
        }

        Long userId = jwtUtil.extractUserId(token);
        String tokenDeviceId = jwtUtil.extractDeviceId(token);
        if (userId == null) {
            return error("CONNECT_UNAUTHORIZED", "유효하지 않은 토큰입니다.", false);
        }

        if (tokenDeviceId == null || !tokenDeviceId.equals(request.deviceId())) {
            return error("CONNECT_INVALID_PAYLOAD", "deviceId가 토큰 정보와 일치하지 않습니다.", false);
        }

        String existingSessionId = socketSessionStore.findSessionId(userId, request.deviceId());
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            return error("CONNECT_DUPLICATE_SESSION", "이미 연결된 세션이 존재합니다.", false);
        }

        OffsetDateTime connectedAt = OffsetDateTime.now(KOREA_ZONE_ID);

        headerAccessor.getSessionAttributes().put("accessToken", request.accessToken());
        headerAccessor.getSessionAttributes().put("deviceId", request.deviceId());
        headerAccessor.getSessionAttributes().put("userId", userId);
        headerAccessor.getSessionAttributes().put("connectedAt", connectedAt.toString());

        socketSessionStore.save(sessionId, userId, request.deviceId(), connectedAt);

        return SocketEventResponse.of(
                SOCKET_CONNECTED_EVENT,
                SocketConnectedResponse.of(
                        sessionId, userId, connectedAt, OffsetDateTime.now(KOREA_ZONE_ID)));
    }

    @MessageMapping("/handshake/disconnect")
    public void disconnect(
            SocketDisconnectRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        if (request == null || isBlank(request.code())) {
            return;
        }

        Object userIdAttribute = headerAccessor.getSessionAttributes().get("userId");
        Object deviceIdAttribute = headerAccessor.getSessionAttributes().get("deviceId");
        if (!(userIdAttribute instanceof Long userId) || !(deviceIdAttribute instanceof String deviceId)) {
            return;
        }

        socketSessionStore.delete(sessionId, userId, deviceId);
    }

    private SocketEventResponse<SocketErrorResponse> error(
            String code, String message, boolean retryable) {
        return SocketEventResponse.of(
                SOCKET_ERROR_EVENT, SocketErrorResponse.of(code, message, retryable));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String stripBearerPrefix(String bearerToken) {
        if (!bearerToken.startsWith("Bearer ")) {
            return null;
        }

        String token = bearerToken.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}
