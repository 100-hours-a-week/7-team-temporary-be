package molip.server.chat.redis.realtime.chatroom.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimeEnvelope;
import molip.server.socket.dto.response.SocketEventResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageCreatedRealtimeEventHandler implements ChatRoomRealtimeEventHandler {

    private static final String EVENT_TYPE = "message.created";

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(ChatRoomRealtimeEnvelope envelope) {
        try {
            ChatMessageCreatedResponse payload =
                    objectMapper.readValue(
                            envelope.payloadJson(), ChatMessageCreatedResponse.class);

            simpMessagingTemplate.convertAndSend(
                    "/sub/room/" + envelope.roomId(), SocketEventResponse.of(EVENT_TYPE, payload));
        } catch (JsonProcessingException ignored) {

        }
    }
}
