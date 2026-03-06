package molip.server.chat.redis.realtime.chatroom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.redis.realtime.chatroom.handler.ChatRoomRealtimeEventHandler;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomRealtimeSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final List<ChatRoomRealtimeEventHandler> handlers;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatRoomRealtimeEnvelope envelope =
                    objectMapper.readValue(body, ChatRoomRealtimeEnvelope.class);

            log.info(
                    "receive room realtime event: eventType={}, roomId={}",
                    envelope.eventType(),
                    envelope.roomId());
            findHandler(envelope.eventType()).ifPresent(handler -> handler.handle(envelope));
        } catch (JsonProcessingException ignored) {

        }
    }

    private Optional<ChatRoomRealtimeEventHandler> findHandler(String eventType) {
        return handlers.stream()
                .filter(handler -> handler.eventType().equals(eventType))
                .findFirst();
    }
}
