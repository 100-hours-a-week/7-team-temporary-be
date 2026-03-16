package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.VideoParticipantOnlineEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoParticipantOnlineAfterCommitEventHandler {

    private static final String EVENT_TYPE = "video.participant.online";

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(VideoParticipantOnlineEvent event) {
        log.info("handle video participant online event after commit: roomId={}", event.roomId());
        chatRoomRealtimePublisher.publish(EVENT_TYPE, event.roomId(), event.payload());
    }
}
