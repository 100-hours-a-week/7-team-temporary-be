package molip.server.chat.event.handler;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.ChatParticipantJoinedResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.event.ChatRoomParticipantEnteredEvent;
import molip.server.chat.realtime.ChatRoomRealtimePublisher;
import molip.server.chat.service.ChatMessageService;
import molip.server.common.enums.MessageType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatRoomParticipantEnteredEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatMessageService chatMessageService;
    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatRoomParticipantEnteredEvent event) {
        OffsetDateTime joinedAt = toKst(event.participant().getCreatedAt());
        String eventId = UUID.randomUUID().toString();

        chatRoomRealtimePublisher.publish(
                "participant.joined",
                event.chatRoom().getId(),
                ChatParticipantJoinedResponse.of(
                        eventId,
                        event.chatRoom().getId(),
                        event.participant().getId(),
                        event.user().getId(),
                        event.user().getNickname(),
                        joinedAt));

        ChatMessage systemMessage =
                chatMessageService.createSystemMessage(
                        event.chatRoom(), event.user().getNickname() + "님이 입장하였습니다.");

        chatRoomRealtimePublisher.publish(
                "message.created",
                event.chatRoom().getId(),
                ChatMessageCreatedResponse.of(
                        UUID.randomUUID().toString(),
                        systemMessage.getId(),
                        event.chatRoom().getId(),
                        MessageType.SYSTEM,
                        systemMessage.getSenderType(),
                        systemMessage.getSenderId(),
                        systemMessage.getContent(),
                        List.of(),
                        toKst(systemMessage.getSentAt())));
    }

    private OffsetDateTime toKst(java.time.LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
