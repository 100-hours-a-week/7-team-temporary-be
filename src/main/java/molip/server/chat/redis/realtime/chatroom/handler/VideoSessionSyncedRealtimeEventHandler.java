package molip.server.chat.redis.realtime.chatroom.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.VideoSessionSyncedResponse;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimeEnvelope;
import molip.server.socket.dto.response.SocketEventResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoSessionSyncedRealtimeEventHandler implements ChatRoomRealtimeEventHandler {

    private static final String EVENT_TYPE = "video.session.synced";

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(ChatRoomRealtimeEnvelope envelope) {
        try {
            VideoSessionSyncedResponse payload =
                    objectMapper.readValue(
                            envelope.payloadJson(), VideoSessionSyncedResponse.class);

            log.info(
                    "broadcast room event: eventType={}, roomId={}, participantId={}",
                    EVENT_TYPE,
                    envelope.roomId(),
                    payload.participantId());
            simpMessagingTemplate.convertAndSend(
                    "/sub/room/" + envelope.roomId(), SocketEventResponse.of(EVENT_TYPE, payload));
        } catch (JsonProcessingException ignored) {

        }
    }
}
