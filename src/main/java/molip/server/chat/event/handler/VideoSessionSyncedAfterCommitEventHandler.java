package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.VideoSessionSyncedEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoSessionSyncedAfterCommitEventHandler {

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VideoSessionSyncedEvent event) {
        log.info(
                "handle video session synced event after commit: roomId={}, eventType={}",
                event.roomId(),
                event.eventType());

        chatRoomRealtimePublisher.publish(event.eventType(), event.roomId(), event.payload());
    }
}
