package molip.server.chat.event.handler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatRoomDeletedCommittedEvent;
import molip.server.chat.event.ChatRoomDeletedEvent;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.socket.dto.response.SocketRoomDeletedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomDeletedEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatRoomDeletedEvent event) {
        log.info("handle room deleted event: roomId={}", event.chatRoom().getId());

        List<Long> targetUserIds =
                chatRoomParticipantService.getActiveParticipants(event.chatRoom().getId()).stream()
                        .map(ChatRoomParticipant::getUser)
                        .map(user -> user.getId())
                        .distinct()
                        .toList();

        SocketRoomDeletedResponse payload =
                SocketRoomDeletedResponse.of(
                        UUID.randomUUID().toString(),
                        event.chatRoom().getId(),
                        toKst(event.chatRoom().getDeletedAt()));

        eventPublisher.publishEvent(new ChatRoomDeletedCommittedEvent(targetUserIds, payload));
    }

    private OffsetDateTime toKst(LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
