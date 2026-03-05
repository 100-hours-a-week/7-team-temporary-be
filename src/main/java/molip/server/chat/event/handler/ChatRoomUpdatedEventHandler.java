package molip.server.chat.event.handler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatRoomUpdatedCommittedEvent;
import molip.server.chat.event.ChatRoomUpdatedEvent;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.socket.dto.response.SocketRoomUpdatedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomUpdatedEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatRoomUpdatedEvent event) {
        log.info("handle room updated event: roomId={}", event.chatRoom().getId());

        List<Long> targetUserIds =
                chatRoomParticipantService.getActiveParticipants(event.chatRoom().getId()).stream()
                        .map(ChatRoomParticipant::getUser)
                        .map(user -> user.getId())
                        .distinct()
                        .toList();

        SocketRoomUpdatedResponse payload =
                SocketRoomUpdatedResponse.of(
                        UUID.randomUUID().toString(),
                        event.chatRoom().getId(),
                        event.chatRoom().getTitle(),
                        event.chatRoom().getDescription(),
                        event.chatRoom().getMaxParticipants(),
                        toKst(event.chatRoom().getUpdatedAt()));

        eventPublisher.publishEvent(new ChatRoomUpdatedCommittedEvent(targetUserIds, payload));
    }

    private OffsetDateTime toKst(LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
