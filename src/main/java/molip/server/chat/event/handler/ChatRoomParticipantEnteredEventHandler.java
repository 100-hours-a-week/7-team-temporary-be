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
import molip.server.chat.dto.response.ChatParticipantJoinedResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatMessageRealtimePayloadFactory;
import molip.server.chat.event.ChatRoomParticipantEnteredCommittedEvent;
import molip.server.chat.event.ChatRoomParticipantEnteredEvent;
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
public class ChatRoomParticipantEnteredEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final ChatRoomQueryFacade chatRoomQueryFacade;
    private final ChatMessageRealtimePayloadFactory chatMessageRealtimePayloadFactory;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatRoomParticipantEnteredEvent event) {
        OffsetDateTime joinedAt = toKst(event.participant().getCreatedAt());
        String eventId = UUID.randomUUID().toString();
        log.info(
                "handle participant entered event: roomId={}, participantId={}, userId={}",
                event.chatRoom().getId(),
                event.participant().getId(),
                event.user().getId());

        ChatParticipantJoinedResponse participantJoined =
                ChatParticipantJoinedResponse.of(
                        eventId,
                        event.chatRoom().getId(),
                        event.participant().getId(),
                        event.user().getId(),
                        event.user().getNickname(),
                        joinedAt);

        ChatMessage systemMessage =
                chatMessageService.createSystemMessage(
                        event.chatRoom(), event.user().getNickname() + "님이 입장하였습니다.");
        log.info(
                "create system message for enter: roomId={}, messageId={}",
                event.chatRoom().getId(),
                systemMessage.getId());

        List<ChatRoomParticipant> activeParticipants =
                chatRoomParticipantService.getActiveParticipants(event.chatRoom().getId());
        int participantsCount = activeParticipants.size();
        ChatMessage latestVisibleMessage =
                chatMessageService.getLatestNonSystemMessage(event.chatRoom().getId()).orElse(null);

        ChatMessageCreatedResponse messageCreated =
                chatMessageRealtimePayloadFactory.buildMessageCreated(systemMessage, List.of());

        List<SocketUnreadChangedResponse> unreadChanges =
                activeParticipants.stream()
                        .filter(
                                participant ->
                                        !participant.getUser().getId().equals(event.user().getId()))
                        .map(
                                participant ->
                                        buildUnreadChanged(
                                                event.chatRoom().getId(),
                                                participant,
                                                latestVisibleMessage,
                                                participantsCount))
                        .toList();

        eventPublisher.publishEvent(
                new ChatRoomParticipantEnteredCommittedEvent(
                        event.chatRoom().getId(),
                        participantJoined,
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
                "publish unreadChanged for enter: roomId={}, targetUserId={}, unreadCount={}",
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
