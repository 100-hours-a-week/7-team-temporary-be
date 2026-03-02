package molip.server.chat.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.ChatParticipantJoinedResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomRealtimeSubscriber implements MessageListener {

    private static final String PARTICIPANT_JOINED_EVENT = "participant.joined";
    private static final String MESSAGE_CREATED_EVENT = "message.created";

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatRoomRealtimeEnvelope envelope =
                    objectMapper.readValue(body, ChatRoomRealtimeEnvelope.class);

            if (PARTICIPANT_JOINED_EVENT.equals(envelope.eventType())) {
                ChatParticipantJoinedResponse payload =
                        objectMapper.readValue(
                                envelope.payloadJson(), ChatParticipantJoinedResponse.class);
                broadcast(envelope.roomId(), PARTICIPANT_JOINED_EVENT, payload);
                return;
            }

            if (MESSAGE_CREATED_EVENT.equals(envelope.eventType())) {
                ChatMessageCreatedResponse payload =
                        objectMapper.readValue(
                                envelope.payloadJson(), ChatMessageCreatedResponse.class);
                broadcast(envelope.roomId(), MESSAGE_CREATED_EVENT, payload);
            }
        } catch (JsonProcessingException ignored) {

        }
    }

    private void broadcast(Long roomId, String event, Object payload) {
        simpMessagingTemplate.convertAndSend(
                "/sub/room/" + roomId, SocketEventResponse.of(event, payload));
    }
}
