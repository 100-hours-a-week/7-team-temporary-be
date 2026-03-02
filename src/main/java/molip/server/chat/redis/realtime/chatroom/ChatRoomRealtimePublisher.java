package molip.server.chat.redis.realtime.chatroom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomRealtimePublisher {

    public static final String CHAT_ROOM_EVENTS_TOPIC = "chat:room-events";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String eventType, Long roomId, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String envelopeJson =
                    objectMapper.writeValueAsString(
                            ChatRoomRealtimeEnvelope.of(eventType, roomId, payloadJson));

            redisTemplate.convertAndSend(CHAT_ROOM_EVENTS_TOPIC, envelopeJson);
        } catch (JsonProcessingException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
