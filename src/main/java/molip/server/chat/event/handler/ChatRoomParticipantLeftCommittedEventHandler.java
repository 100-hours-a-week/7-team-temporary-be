package molip.server.chat.event.handler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.VideoRoomDeletedResponse;
import molip.server.chat.event.ChatRoomParticipantLeftCommittedEvent;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.common.enums.ChatRoomType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomParticipantLeftCommittedEventHandler {

    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;
    private final ChatUserRealtimePublisher chatUserRealtimePublisher;
    private final ChatRoomParticipantService chatRoomParticipantService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomParticipantLeftCommittedEvent event) {
        log.info("handle participant left committed event: roomId={}", event.roomId());

        if (event.roomType() == ChatRoomType.CAM_STUDY) {
            chatRoomRealtimePublisher.publish(
                    "video.participant.left", event.roomId(), event.participantLeft());
        } else {
            chatRoomRealtimePublisher.publish(
                    "participant.left", event.roomId(), event.participantLeft());
        }
        chatRoomRealtimePublisher.publish(
                "message.created", event.roomId(), event.messageCreated());

        event.unreadChanges()
                .forEach(
                        unreadChanged ->
                                chatUserRealtimePublisher.publishUnreadChanged(
                                        unreadChanged.userId(), unreadChanged));

        if (event.roomType() == ChatRoomType.CAM_STUDY && isNoActiveParticipant(event.roomId())) {
            chatRoomRealtimePublisher.publish(
                    "video.room.deleted",
                    event.roomId(),
                    VideoRoomDeletedResponse.of(
                            UUID.randomUUID().toString(),
                            event.roomId(),
                            OffsetDateTime.now(ZoneOffset.of("+09:00")),
                            "NO_ACTIVE_PARTICIPANTS"));
        }
    }

    private boolean isNoActiveParticipant(Long roomId) {
        int activeCount =
                chatRoomParticipantService
                        .countActiveParticipantsByChatRoomIds(List.of(roomId))
                        .getOrDefault(roomId, 0);

        return activeCount == 0;
    }
}
