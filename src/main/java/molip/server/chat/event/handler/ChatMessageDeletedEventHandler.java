package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatMessageDeletedCommittedEvent;
import molip.server.chat.event.ChatMessageDeletedEvent;
import molip.server.chat.event.ChatMessageRealtimePayloadFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageDeletedEventHandler {

    private final ChatMessageRealtimePayloadFactory chatMessageRealtimePayloadFactory;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatMessageDeletedEvent event) {
        log.info(
                "handle chat message deleted event: roomId={}, messageId={}",
                event.message().getChatRoom().getId(),
                event.message().getId());

        eventPublisher.publishEvent(
                new ChatMessageDeletedCommittedEvent(
                        event.message().getChatRoom().getId(),
                        chatMessageRealtimePayloadFactory.buildMessageDeleted(event.message())));
    }
}
