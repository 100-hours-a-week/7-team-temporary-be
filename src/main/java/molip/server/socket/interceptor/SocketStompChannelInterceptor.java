package molip.server.socket.interceptor;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketStompChannelInterceptor implements ChannelInterceptor {
    private static final String ACCESS_TOKEN_SESSION_KEY = "accessToken";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final RedisSocketSessionStore socketSessionStore;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            saveAccessTokenToSession(accessor);
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            cleanup(accessor);
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private void cleanup(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }

        Object userIdAttribute = sessionAttributes.get("userId");
        Object deviceIdAttribute = sessionAttributes.get("deviceId");
        if (!(userIdAttribute instanceof Long userId)
                || !(deviceIdAttribute instanceof String deviceId)) {
            return;
        }

        socketSessionStore.delete(accessor.getSessionId(), userId, deviceId);
    }

    private void saveAccessTokenToSession(StompHeaderAccessor accessor) {
        String token = resolveAccessTokenFromCookie(accessor.getFirstNativeHeader("cookie"));
        if (token == null) {
            return;
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            sessionAttributes = new HashMap<>();
            accessor.setSessionAttributes(sessionAttributes);
        }

        sessionAttributes.put(ACCESS_TOKEN_SESSION_KEY, token);
    }

    private String resolveAccessTokenFromCookie(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
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
