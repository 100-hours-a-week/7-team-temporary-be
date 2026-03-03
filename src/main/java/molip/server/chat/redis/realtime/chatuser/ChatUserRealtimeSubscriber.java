package molip.server.chat.redis.realtime.chatuser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.chat.redis.realtime.chatuser.handler.ChatUserRealtimeEventHandler;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatUserRealtimeSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final List<ChatUserRealtimeEventHandler> handlers;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatUserRealtimeEnvelope envelope =
                    objectMapper.readValue(body, ChatUserRealtimeEnvelope.class);

            findHandler(envelope.eventType()).ifPresent(handler -> handler.handle(envelope));
        } catch (JsonProcessingException ignored) {

        }
    }

    private Optional<ChatUserRealtimeEventHandler> findHandler(String eventType) {
        return handlers.stream()
                .filter(handler -> handler.eventType().equals(eventType))
                .findFirst();
    }
}
