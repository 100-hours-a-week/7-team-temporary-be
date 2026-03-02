package molip.server.socket.interceptor;

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
    private final RedisSocketSessionStore socketSessionStore;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            cleanup(accessor);
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private void cleanup(StompHeaderAccessor accessor) {
        Object userIdAttribute = accessor.getSessionAttributes().get("userId");
        Object deviceIdAttribute = accessor.getSessionAttributes().get("deviceId");
        if (!(userIdAttribute instanceof Long userId) || !(deviceIdAttribute instanceof String deviceId)) {
            return;
        }

        socketSessionStore.delete(accessor.getSessionId(), userId, deviceId);
    }

}
