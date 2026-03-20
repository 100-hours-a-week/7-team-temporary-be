package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatRoomParticipantEnteredCommittedEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import molip.server.common.enums.ChatRoomType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomParticipantEnteredCommittedEventHandler {

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;
    private final ChatUserRealtimePublisher chatUserRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomParticipantEnteredCommittedEvent event) {
        log.info("handle participant entered committed event: roomId={}", event.roomId());

        if (event.roomType() != ChatRoomType.CAM_STUDY) {
            chatRoomRealtimePublisher.publish(
                    "participant.joined", event.roomId(), event.participantJoined());
        }
        chatRoomRealtimePublisher.publish(
                "message.created", event.roomId(), event.messageCreated());

        event.unreadChanges()
                .forEach(
                        unreadChanged ->
                                chatUserRealtimePublisher.publishUnreadChanged(
                                        unreadChanged.userId(), unreadChanged));
    }
}
