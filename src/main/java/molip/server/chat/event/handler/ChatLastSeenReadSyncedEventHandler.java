package molip.server.chat.event.handler;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatLastSeenReadSyncedCommittedEvent;
import molip.server.chat.event.ChatLastSeenReadSyncedEvent;
import molip.server.chat.facade.ChatRoomQueryFacade;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatLastSeenReadSyncedEventHandler {

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final ChatRoomQueryFacade chatRoomQueryFacade;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatLastSeenReadSyncedEvent event) {
        ChatRoomParticipant participant =
                chatRoomParticipantService.findById(event.participantId()).orElse(null);

        if (participant == null) {
            return;
        }

        chatRoomParticipantService.updateLastSeenMessageId(participant, event.lastSeenMessageId());

        ChatMessage latestVisibleMessage =
                chatMessageService.getLatestNonSystemMessage(event.roomId()).orElse(null);

        int participantsCount =
                chatRoomParticipantService
                        .countActiveParticipantsByChatRoomIds(List.of(event.roomId()))
                        .getOrDefault(event.roomId(), 0);

        ChatMyRoomItemResponse row =
                chatRoomQueryFacade.buildMyChatRoomItem(
                        participant, latestVisibleMessage, participantsCount);

        SocketUnreadChangedResponse unreadChanged =
                SocketUnreadChangedResponse.of(
                        UUID.randomUUID().toString(),
                        event.userId(),
                        event.roomId(),
                        row.unreadCount(),
                        row.lastUserMessagePreview(),
                        row.lastUserMessageSentAt(),
                        row.participantsCount());

        eventPublisher.publishEvent(
                new ChatLastSeenReadSyncedCommittedEvent(event.userId(), unreadChanged));
    }
}
