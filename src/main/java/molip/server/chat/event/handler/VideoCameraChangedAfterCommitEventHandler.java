package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.VideoCameraChangedEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoCameraChangedAfterCommitEventHandler {

    private static final String EVENT_TYPE = "video.camera.changed";

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VideoCameraChangedEvent event) {
        log.info(
                "handle video camera changed event after commit: roomId={}, participantId={}, userId={}, cameraEnabled={}",
                event.roomId(),
                event.payload().participantId(),
                event.payload().userId(),
                event.payload().cameraEnabled());

        chatRoomRealtimePublisher.publish(EVENT_TYPE, event.roomId(), event.payload());
    }
}
