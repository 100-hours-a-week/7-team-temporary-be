package molip.server.chat.redis.realtime.chatuser.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.auth.store.redis.RedisDeviceStore;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimeEnvelope;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import molip.server.socket.service.SocketUserChannelBroadcaster;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatUnreadChangedRealtimeEventHandler implements ChatUserRealtimeEventHandler {

    private static final String EVENT_TYPE = "unreadChanged";

    private final ObjectMapper objectMapper;
    private final RedisDeviceStore deviceStore;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketUserChannelBroadcaster socketUserChannelBroadcaster;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(ChatUserRealtimeEnvelope envelope) {
        try {
            SocketUnreadChangedResponse payload =
                    objectMapper.readValue(
                            envelope.payloadJson(), SocketUnreadChangedResponse.class);

            Set<String> deviceIds = deviceStore.listDevices(envelope.userId());
            log.info(
                    "broadcast user event: eventType={}, userId={}, deviceCount={}",
                    EVENT_TYPE,
                    envelope.userId(),
                    deviceIds.size());

            for (String deviceId : deviceIds) {
                String sessionId = socketSessionStore.findSessionId(envelope.userId(), deviceId);
                if (sessionId == null || sessionId.isBlank()) {
                    continue;
                }

                log.info(
                        "send unreadChanged to session: userId={}, deviceId={}, sessionId={}, roomId={}",
                        envelope.userId(),
                        deviceId,
                        sessionId,
                        payload.roomId());
                socketUserChannelBroadcaster.sendUnreadChanged(sessionId, payload);
            }
        } catch (JsonProcessingException ignored) {

        }
    }
}
