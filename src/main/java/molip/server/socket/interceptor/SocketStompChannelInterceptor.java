package molip.server.socket.interceptor;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketStompChannelInterceptor implements ChannelInterceptor {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final RedisSocketSessionStore socketSessionStore;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            cleanup(accessor);
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("accessToken");
        String deviceId = accessor.getFirstNativeHeader("deviceId");

        if (bearerToken == null || bearerToken.isBlank() || deviceId == null || deviceId.isBlank()) {
            throw new MessagingException("CONNECT_INVALID_PAYLOAD: accessToken 또는 deviceId가 누락되었습니다.");
        }

        String token = stripBearerPrefix(bearerToken);
        if (token == null) {
            throw new MessagingException("CONNECT_INVALID_PAYLOAD: accessToken 형식이 올바르지 않습니다.");
        }

        JwtValidationStatus tokenStatus = jwtTokenProvider.getAccessTokenStatus(token);
        if (tokenStatus == JwtValidationStatus.EXPIRED) {
            throw new MessagingException("CONNECT_TOKEN_EXPIRED: 액세스 토큰이 만료되었습니다.");
        }

        if (tokenStatus == JwtValidationStatus.INVALID) {
            throw new MessagingException("CONNECT_UNAUTHORIZED: 유효하지 않은 토큰입니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        String tokenDeviceId = jwtUtil.extractDeviceId(token);
        if (userId == null) {
            throw new MessagingException("CONNECT_UNAUTHORIZED: 유효하지 않은 토큰입니다.");
        }

        if (tokenDeviceId == null || !tokenDeviceId.equals(deviceId)) {
            throw new MessagingException("CONNECT_INVALID_PAYLOAD: deviceId가 토큰 정보와 일치하지 않습니다.");
        }

        String sessionId = accessor.getSessionId();
        String existingSessionId = socketSessionStore.findSessionId(userId, deviceId);
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            throw new MessagingException("CONNECT_DUPLICATE_SESSION: 이미 연결된 세션이 존재합니다.");
        }

        OffsetDateTime connectedAt = OffsetDateTime.now(KOREA_ZONE_ID);

        accessor.getSessionAttributes().put("accessToken", bearerToken);
        accessor.getSessionAttributes().put("deviceId", deviceId);
        accessor.getSessionAttributes().put("userId", userId);
        accessor.getSessionAttributes().put("connectedAt", connectedAt.toString());

        Principal principal =
                new UsernamePasswordAuthenticationToken(userId.toString(), null, java.util.List.of());
        accessor.setUser(principal);

        socketSessionStore.save(sessionId, userId, deviceId, connectedAt);
    }

    private void cleanup(StompHeaderAccessor accessor) {
        Object userIdAttribute = accessor.getSessionAttributes().get("userId");
        Object deviceIdAttribute = accessor.getSessionAttributes().get("deviceId");
        if (!(userIdAttribute instanceof Long userId) || !(deviceIdAttribute instanceof String deviceId)) {
            return;
        }

        socketSessionStore.delete(accessor.getSessionId(), userId, deviceId);
    }

    private String stripBearerPrefix(String bearerToken) {
        if (!bearerToken.startsWith("Bearer ")) {
            return null;
        }

        String token = bearerToken.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}
