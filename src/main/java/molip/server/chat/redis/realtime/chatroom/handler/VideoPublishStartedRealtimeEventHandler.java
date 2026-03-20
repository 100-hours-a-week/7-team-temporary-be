package molip.server.chat.redis.realtime.chatroom.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.VideoPublishChangedResponse;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimeEnvelope;
import molip.server.socket.dto.response.SocketEventResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoPublishStartedRealtimeEventHandler implements ChatRoomRealtimeEventHandler {

    private static final String EVENT_TYPE = "video.publish.started";

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(ChatRoomRealtimeEnvelope envelope) {
        try {
            VideoPublishChangedResponse payload =
                    objectMapper.readValue(
                            envelope.payloadJson(), VideoPublishChangedResponse.class);

            log.info(
                    "broadcast room event: eventType={}, roomId={}, participantId={}, userId={}",
                    EVENT_TYPE,
                    envelope.roomId(),
                    payload.participantId(),
                    payload.userId());
            simpMessagingTemplate.convertAndSend(
                    "/sub/room/" + envelope.roomId(), SocketEventResponse.of(EVENT_TYPE, payload));
        } catch (JsonProcessingException ignored) {

        }
    }
}
