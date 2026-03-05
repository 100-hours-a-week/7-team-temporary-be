package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatRoomParticipantLeftCommittedEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomParticipantLeftCommittedEventHandler {

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;
    private final ChatUserRealtimePublisher chatUserRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomParticipantLeftCommittedEvent event) {
        log.info("handle participant left committed event: roomId={}", event.roomId());

        chatRoomRealtimePublisher.publish(
                "participant.left", event.roomId(), event.participantLeft());
        chatRoomRealtimePublisher.publish(
                "message.created", event.roomId(), event.messageCreated());

        event.unreadChanges()
                .forEach(
                        unreadChanged ->
                                chatUserRealtimePublisher.publishUnreadChanged(
                                        unreadChanged.userId(), unreadChanged));
    }
}
