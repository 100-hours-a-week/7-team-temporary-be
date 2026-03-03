package molip.server.chat.redis.realtime.chatuser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatUserRealtimePublisher {

    public static final String CHAT_USER_EVENTS_TOPIC = "chat:user-events";

    private static final String UNREAD_CHANGED_EVENT = "unreadChanged";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishUnreadChanged(Long userId, SocketUnreadChangedResponse payload) {
        publish(UNREAD_CHANGED_EVENT, userId, payload);
    }

    public void publish(String eventType, Long userId, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String envelopeJson =
                    objectMapper.writeValueAsString(
                            ChatUserRealtimeEnvelope.of(eventType, userId, payloadJson));

            log.info("publish user realtime event: eventType={}, userId={}", eventType, userId);
            redisTemplate.convertAndSend(CHAT_USER_EVENTS_TOPIC, envelopeJson);
        } catch (JsonProcessingException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
