package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatRoomDeletedCommittedEvent;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomDeletedCommittedEventHandler {

    private final ChatUserRealtimePublisher chatUserRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomDeletedCommittedEvent event) {
        log.info("handle room deleted committed event: roomId={}", event.roomDeleted().roomId());

        event.targetUserIds()
                .forEach(
                        userId ->
                                chatUserRealtimePublisher.publish(
                                        "room.deleted", userId, event.roomDeleted()));
    }
}
