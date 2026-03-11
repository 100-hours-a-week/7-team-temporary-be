package molip.server.chat.event.handler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.dto.response.ChatParticipantLeftResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatMessageRealtimePayloadFactory;
import molip.server.chat.event.ChatRoomParticipantLeftCommittedEvent;
import molip.server.chat.event.ChatRoomParticipantLeftEvent;
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
public class ChatRoomParticipantLeftEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final ChatRoomQueryFacade chatRoomQueryFacade;
    private final ChatMessageRealtimePayloadFactory chatMessageRealtimePayloadFactory;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatRoomParticipantLeftEvent event) {
        String eventId = UUID.randomUUID().toString();
        log.info(
                "handle participant left event: roomId={}, participantId={}, userId={}",
                event.chatRoom().getId(),
                event.participant().getId(),
                event.user().getId());

        ChatMessage systemMessage =
                chatMessageService.createSystemMessage(
                        event.chatRoom(), event.user().getNickname() + "님이 퇴장하였습니다.");
        ChatParticipantLeftResponse participantLeft =
                ChatParticipantLeftResponse.of(
                        eventId,
                        event.chatRoom().getId(),
                        event.participant().getId(),
                        event.user().getId(),
                        toKst(event.participant().getLeftAt()));

        List<ChatRoomParticipant> activeParticipants =
                chatRoomParticipantService.getActiveParticipants(event.chatRoom().getId());

        int participantsCount = activeParticipants.size();
        ChatMessage latestVisibleMessage =
                chatMessageService.getLatestNonSystemMessage(event.chatRoom().getId()).orElse(null);

        List<SocketUnreadChangedResponse> unreadChanges =
                activeParticipants.stream()
                        .map(
                                participant ->
                                        buildUnreadChanged(
                                                event.chatRoom().getId(),
                                                participant,
                                                latestVisibleMessage,
                                                participantsCount))
                        .toList();
        ChatMessageCreatedResponse messageCreated =
                chatMessageRealtimePayloadFactory.buildMessageCreated(systemMessage, List.of());

        log.info(
                "create system message for leave: roomId={}, messageId={}",
                event.chatRoom().getId(),
                systemMessage.getId());

        eventPublisher.publishEvent(
                new ChatRoomParticipantLeftCommittedEvent(
                        event.chatRoom().getId(),
                        event.chatRoom().getType(),
                        participantLeft,
                        messageCreated,
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
                "publish unreadChanged for leave: roomId={}, targetUserId={}, unreadCount={}",
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

    private OffsetDateTime toKst(LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
