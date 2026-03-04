package molip.server.chat.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.event.ChatMessageRealtimePayloadFactory;
import molip.server.chat.event.ChatMessageUpdatedCommittedEvent;
import molip.server.chat.event.ChatMessageUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageUpdatedEventHandler {

    private final ChatMessageRealtimePayloadFactory chatMessageRealtimePayloadFactory;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatMessageUpdatedEvent event) {
        log.info(
                "handle chat message updated event: roomId={}, messageId={}",
                event.message().getChatRoom().getId(),
                event.message().getId());

        eventPublisher.publishEvent(
                new ChatMessageUpdatedCommittedEvent(
                        event.message().getChatRoom().getId(),
                        chatMessageRealtimePayloadFactory.buildMessageUpdated(event.message())));
    }
}
