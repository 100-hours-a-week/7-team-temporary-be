package molip.server.socket.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketPongRequest;
import molip.server.socket.dto.request.SocketUserSubscribeRequest;
import molip.server.socket.dto.response.SocketConnectedResponse;
import molip.server.socket.dto.response.SocketErrorResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketSubscribedUserResponse;
import molip.server.socket.session.SocketSessionContext;
import molip.server.socket.session.SocketSessionSupport;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocketHandshakeService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String ACCESS_TOKEN_SESSION_KEY = "accessToken";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String SOCKET_CONNECTED_EVENT = "socket.connected";
    private static final String SOCKET_ERROR_EVENT = "socket.error";
    private static final String SUBSCRIBED_USER_EVENT = "subscribed.user";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketHandshakeChannelBroadcaster socketHandshakeChannelBroadcaster;
    private final SocketSessionSupport socketSessionSupport;

    @Transactional
    public SocketEventResponse<?> connect(
            SocketConnectRequest request,
            String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        SocketEventResponse<?> invalidRequestResponse = validateConnectRequest(request);

        if (invalidRequestResponse != null) {
            return invalidRequestResponse;
        }

        String token = resolveAccessToken(headerAccessor);

        JwtValidationStatus tokenStatus = jwtTokenProvider.getAccessTokenStatus(token);
        SocketEventResponse<?> invalidTokenStatusResponse = validateTokenStatus(tokenStatus);

        if (invalidTokenStatusResponse != null) {
            return invalidTokenStatusResponse;
        }

        Long userId = jwtUtil.extractUserId(token);
        String tokenDeviceId = jwtUtil.extractDeviceId(token);
        SocketEventResponse<?> invalidTokenPayloadResponse =
                validateTokenPayload(userId, tokenDeviceId, request.deviceId());

        if (invalidTokenPayloadResponse != null) {
            return invalidTokenPayloadResponse;
        }

        String existingSessionId = socketSessionStore.findSessionId(userId, request.deviceId());
        SocketEventResponse<?> duplicateSessionResponse =
                handleDuplicateSession(existingSessionId, sessionId);

        if (duplicateSessionResponse != null) {
            socketHandshakeChannelBroadcaster.sendDisconnect(
                    existingSessionId, "DUPLICATE_SESSION", "새로운 세션 연결을 위해 기존 세션을 종료합니다.");

            socketSessionStore.delete(existingSessionId, userId, request.deviceId());

            socketHandshakeChannelBroadcaster.sendSessionReleased(userId, existingSessionId, true);

            return duplicateSessionResponse;
        }

        OffsetDateTime connectedAt = OffsetDateTime.now(KOREA_ZONE_ID);
        SocketSessionContext sessionContext =
                SocketSessionContext.of(token, request.deviceId(), userId, connectedAt);

        socketSessionSupport.setSessionContext(headerAccessor, sessionContext);
        socketSessionStore.save(sessionId, userId, request.deviceId(), connectedAt);
        socketHandshakeChannelBroadcaster.sendPing(sessionId, connectedAt);

        return SocketEventResponse.of(
                SOCKET_CONNECTED_EVENT,
                SocketConnectedResponse.of(
                        sessionId, userId, connectedAt, OffsetDateTime.now(KOREA_ZONE_ID)));
    }

    public void disconnect(String sessionId, SocketSessionContext sessionContext) {
        socketHandshakeChannelBroadcaster.sendDisconnect(sessionId, "LOGOUT", "로그아웃으로 연결을 종료합니다.");

        socketSessionStore.delete(sessionId, sessionContext.userId(), sessionContext.deviceId());

        socketHandshakeChannelBroadcaster.sendSessionReleased(
                sessionContext.userId(), sessionId, true);
    }

    public void pong(SocketPongRequest request, SimpMessageHeaderAccessor headerAccessor) {
        OffsetDateTime lastPongAt = resolveLastPongAt(request);

        socketSessionSupport.updateLastPongAt(headerAccessor, lastPongAt);
    }

    public void requireReconnect(String sessionId) {
        socketHandshakeChannelBroadcaster.sendReconnectRequired(
                sessionId, "TOKEN_EXPIRED", "세션이 만료되었거나 유효하지 않습니다. 토큰 갱신 후 재연결이 필요합니다.", 1000L);
    }

    public SocketEventResponse<?> subscribeUser(
            SocketUserSubscribeRequest request, SocketSessionContext sessionContext) {
        OffsetDateTime subscribedAt = resolveSubscribedAt(request);

        return SocketEventResponse.of(
                SUBSCRIBED_USER_EVENT,
                SocketSubscribedUserResponse.of(sessionContext.userId(), subscribedAt));
    }

    public SocketEventResponse<SocketErrorResponse> invalidSubscribeState() {
        return error("CHANNEL_SUBSCRIBE_INVALID_STATE", "인증 완료 후에만 개인 채널을 구독할 수 있습니다.", false);
    }

    private SocketEventResponse<SocketErrorResponse> error(
            String code, String message, boolean retryable) {
        return SocketEventResponse.of(
                SOCKET_ERROR_EVENT, SocketErrorResponse.of(code, message, retryable));
    }

    private SocketEventResponse<?> validateConnectRequest(SocketConnectRequest request) {
        if (request == null || isBlank(request.deviceId())) {
            return error("CONNECT_INVALID_PAYLOAD", "deviceId가 누락되었습니다.", false);
        }

        return null;
    }

    private SocketEventResponse<?> validateTokenStatus(JwtValidationStatus tokenStatus) {
        if (tokenStatus == JwtValidationStatus.EXPIRED) {
            return error("CONNECT_TOKEN_EXPIRED", "액세스 토큰이 만료되었습니다.", true);
        }

        if (tokenStatus == JwtValidationStatus.INVALID) {
            return error("CONNECT_UNAUTHORIZED", "유효하지 않은 토큰입니다.", false);
        }

        return null;
    }

    private SocketEventResponse<?> validateTokenPayload(
            Long userId, String tokenDeviceId, String requestDeviceId) {
        if (userId == null) {
            return error("CONNECT_UNAUTHORIZED", "유효하지 않은 토큰입니다.", false);
        }

        if (tokenDeviceId == null || !tokenDeviceId.equals(requestDeviceId)) {
            return error("CONNECT_INVALID_PAYLOAD", "deviceId가 토큰 정보와 일치하지 않습니다.", false);
        }

        return null;
    }

    private SocketEventResponse<?> handleDuplicateSession(
            String existingSessionId, String currentSessionId) {
        if (existingSessionId != null && !existingSessionId.equals(currentSessionId)) {
            return error("CONNECT_DUPLICATE_SESSION", "이미 연결된 세션이 존재합니다.", false);
        }

        return null;
    }

    private OffsetDateTime resolveSubscribedAt(SocketUserSubscribeRequest request) {
        if (request != null && request.requestedAt() != null) {
            return request.requestedAt();
        }

        return OffsetDateTime.now(KOREA_ZONE_ID);
    }

    private OffsetDateTime resolveLastPongAt(SocketPongRequest request) {
        if (request != null && request.timestamp() != null) {
            return request.timestamp();
        }

        return OffsetDateTime.now(KOREA_ZONE_ID);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String resolveAccessToken(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            Object tokenFromSession =
                    headerAccessor.getSessionAttributes().get(ACCESS_TOKEN_SESSION_KEY);
            if (tokenFromSession instanceof String token && !token.isBlank()) {
                return token;
            }
        }

        String cookieHeader = headerAccessor.getFirstNativeHeader("cookie");
        if (isBlank(cookieHeader)) {
            return null;
        }

        String[] cookiePairs = cookieHeader.split(";");
        for (String cookiePair : cookiePairs) {
            String[] nameValue = cookiePair.trim().split("=", 2);
            if (nameValue.length == 2
                    && ACCESS_TOKEN_COOKIE.equals(nameValue[0].trim())
                    && !nameValue[1].trim().isBlank()) {
                return nameValue[1].trim();
            }
        }

        return null;
    }
}
