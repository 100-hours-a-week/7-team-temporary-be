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
import molip.server.chat.dto.response.MessageImageInfoResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatMessageSentCommittedEvent;
import molip.server.chat.event.ChatMessageSentEvent;
import molip.server.chat.facade.ChatRoomQueryFacade;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.common.enums.ImageType;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSentEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatRoomQueryFacade chatRoomQueryFacade;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatMessageSentEvent event) {
        log.info(
                "handle chat message sent event: roomId={}, messageId={}, senderUserId={}",
                event.chatRoom().getId(),
                event.message().getId(),
                event.senderUserId());

        List<ChatRoomParticipant> activeParticipants =
                chatRoomParticipantService.getActiveParticipants(event.chatRoom().getId());
        int participantsCount = activeParticipants.size();

        ChatMessageCreatedResponse messageCreated =
                ChatMessageCreatedResponse.of(
                        UUID.randomUUID().toString(),
                        event.message().getId(),
                        event.chatRoom().getId(),
                        event.message().getMessageType(),
                        event.message().getSenderType(),
                        event.message().getSenderId(),
                        event.message().getContent(),
                        buildMessageImages(event.images()),
                        toKst(event.message().getSentAt()));

        List<SocketUnreadChangedResponse> unreadChanges =
                activeParticipants.stream()
                        .filter(
                                participant ->
                                        !participant.getUser().getId().equals(event.senderUserId()))
                        .map(
                                participant ->
                                        buildUnreadChanged(
                                                event.chatRoom().getId(),
                                                participant,
                                                event.message(),
                                                participantsCount))
                        .toList();

        eventPublisher.publishEvent(
                new ChatMessageSentCommittedEvent(
                        event.chatRoom().getId(), messageCreated, unreadChanges));
    }

    private List<MessageImageInfoResponse> buildMessageImages(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return java.util.stream.IntStream.range(0, images.size())
                .mapToObj(index -> buildMessageImage(images.get(index), index + 1))
                .toList();
    }

    private MessageImageInfoResponse buildMessageImage(Image image, int sortOrder) {
        ImageGetUrlResponse imageResponse =
                imageService.issueGetUrlWithoutValidation(ImageType.MESSAGES, image.getUploadKey());

        return MessageImageInfoResponse.of(
                imageResponse.url(), imageResponse.expiresAt(), image.getUploadKey(), sortOrder);
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
                "publish unreadChanged for message send: roomId={}, targetUserId={}, unreadCount={}",
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
