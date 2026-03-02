package molip.server.chat.redis.realtime.chatuser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.auth.store.redis.RedisDeviceStore;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import molip.server.socket.service.SocketUserChannelBroadcaster;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatUserRealtimeSubscriber implements MessageListener {

    private static final String UNREAD_CHANGED_EVENT = "unreadChanged";

    private final ObjectMapper objectMapper;
    private final RedisDeviceStore deviceStore;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketUserChannelBroadcaster socketUserChannelBroadcaster;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatUserRealtimeEnvelope envelope =
                    objectMapper.readValue(body, ChatUserRealtimeEnvelope.class);

            if (!UNREAD_CHANGED_EVENT.equals(envelope.eventType())) {
                return;
            }

            SocketUnreadChangedResponse payload =
                    objectMapper.readValue(
                            envelope.payloadJson(), SocketUnreadChangedResponse.class);

            broadcastToUserSessions(envelope.userId(), payload);
        } catch (JsonProcessingException ignored) {

        }
    }

    private void broadcastToUserSessions(Long userId, SocketUnreadChangedResponse payload) {
        Set<String> deviceIds = deviceStore.listDevices(userId);

        for (String deviceId : deviceIds) {
            String sessionId = socketSessionStore.findSessionId(userId, deviceId);
            if (sessionId == null || sessionId.isBlank()) {
                continue;
            }

            socketUserChannelBroadcaster.sendUnreadChanged(sessionId, payload);
        }
    }
}
