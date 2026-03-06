package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.chat.event.ChatLastSeenReadSyncedCommittedEvent;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatLastSeenReadSyncedCommittedEventHandler {

    private final ChatUserRealtimePublisher chatUserRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatLastSeenReadSyncedCommittedEvent event) {
        chatUserRealtimePublisher.publishUnreadChanged(event.userId(), event.unreadChanged());
    }
}
