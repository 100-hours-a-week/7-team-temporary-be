package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.VideoTokenIssuedEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTokenIssuedAfterCommitEventHandler {

    private static final String EVENT_TYPE = "video.token.issued";

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VideoTokenIssuedEvent event) {
        log.info("handle video token issued event after commit: roomId={}", event.roomId());

        chatRoomRealtimePublisher.publish(EVENT_TYPE, event.roomId(), event.payload());
    }
}
