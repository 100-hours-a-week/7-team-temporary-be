package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatRoomUpdatedCommittedEvent;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomUpdatedCommittedEventHandler {

    private final ChatUserRealtimePublisher chatUserRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomUpdatedCommittedEvent event) {
        log.info("handle room updated committed event: roomId={}", event.roomUpdated().roomId());

        event.targetUserIds()
                .forEach(
                        userId ->
                                chatUserRealtimePublisher.publish(
                                        "room.updated", userId, event.roomUpdated()));
    }
}
