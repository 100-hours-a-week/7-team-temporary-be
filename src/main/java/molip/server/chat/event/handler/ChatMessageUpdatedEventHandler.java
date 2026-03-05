package molip.server.chat.event.handler;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatMessageRealtimePayloadFactory;
import molip.server.chat.event.ChatMessageUpdatedCommittedEvent;
import molip.server.chat.event.ChatMessageUpdatedEvent;
import molip.server.chat.facade.ChatRoomQueryFacade;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageUpdatedEventHandler {

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final ChatRoomQueryFacade chatRoomQueryFacade;
    private final ChatMessageRealtimePayloadFactory chatMessageRealtimePayloadFactory;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatMessageUpdatedEvent event) {
        log.info(
                "handle chat message updated event: roomId={}, messageId={}",
                event.message().getChatRoom().getId(),
                event.message().getId());

        List<ChatRoomParticipant> activeParticipants =
                chatRoomParticipantService.getActiveParticipants(
                        event.message().getChatRoom().getId());

        int participantsCount = activeParticipants.size();

        ChatMessage latestVisibleMessage =
                chatMessageService
                        .getLatestNonSystemMessage(event.message().getChatRoom().getId())
                        .orElse(null);

        List<SocketUnreadChangedResponse> unreadChanges =
                activeParticipants.stream()
                        .map(
                                participant ->
                                        buildUnreadChanged(
                                                event.message().getChatRoom().getId(),
                                                participant,
                                                latestVisibleMessage,
                                                participantsCount))
                        .toList();

        eventPublisher.publishEvent(
                new ChatMessageUpdatedCommittedEvent(
                        event.message().getChatRoom().getId(),
                        chatMessageRealtimePayloadFactory.buildMessageUpdated(event.message()),
                        unreadChanges));
    }

    private SocketUnreadChangedResponse buildUnreadChanged(
            Long roomId,
            ChatRoomParticipant participant,
            ChatMessage latestMessage,
            int participantsCount) {

        ChatMyRoomItemResponse row =
                chatRoomQueryFacade.buildMyChatRoomItem(
                        participant, latestMessage, participantsCount);

        Long userId = participant.getUser().getId();

        log.info(
                "publish unreadChanged for message update: roomId={}, targetUserId={}, unreadCount={}",
                roomId,
                userId,
                row.unreadCount());

        return SocketUnreadChangedResponse.of(
                UUID.randomUUID().toString(),
                userId,
                roomId,
                row.unreadCount(),
                row.lastUserMessagePreview(),
                row.lastUserMessageSentAt(),
                row.participantsCount());
    }
}
