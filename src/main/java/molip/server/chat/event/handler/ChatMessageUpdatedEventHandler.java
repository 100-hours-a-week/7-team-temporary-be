package molip.server.chat.event.handler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatMessageUpdatedCommittedEvent;
import molip.server.chat.event.ChatMessageUpdatedEvent;
import molip.server.socket.dto.response.SocketMessageUpdatedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageUpdatedEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatMessageUpdatedEvent event) {
        log.info(
                "handle chat message updated event: roomId={}, messageId={}",
                event.message().getChatRoom().getId(),
                event.message().getId());

        SocketMessageUpdatedResponse payload =
                SocketMessageUpdatedResponse.of(
                        UUID.randomUUID().toString(),
                        event.message().getChatRoom().getId(),
                        event.message().getId(),
                        event.message().getContent(),
                        toKst(event.message().getUpdatedAt()));

        eventPublisher.publishEvent(
                new ChatMessageUpdatedCommittedEvent(
                        event.message().getChatRoom().getId(), payload));
    }

    private OffsetDateTime toKst(LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
