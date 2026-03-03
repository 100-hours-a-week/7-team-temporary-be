package molip.server.chat.facade;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.ChatMessageSendRequest;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.dto.response.ChatLastSeenUpdatedResponse;
import molip.server.chat.dto.response.ChatMessageSendCommandResult;
import molip.server.chat.dto.response.ChatMessageSendResponse;
import molip.server.chat.dto.response.ChatRoomEnterResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatRoomParticipantEnteredEvent;
import molip.server.chat.redis.idempotency.ChatMessageIdempotencyRecord;
import molip.server.chat.redis.idempotency.RedisChatMessageIdempotencyStore;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.chat.service.MessageImageService;
import molip.server.common.enums.MessageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatRoomCommandFacade {

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final MessageImageService messageImageService;
    private final UserService userService;
    private final ImageService imageService;
    private final RedisChatMessageIdempotencyStore redisChatMessageIdempotencyStore;
    private final ChatRoomRealtimePublisher chatRoomRealtimePublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoomEnterResponse enterChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
        Users user = userService.getUser(userId);
        Long lastSeenMessageId =
                chatMessageService.getLatestMessage(roomId).map(ChatMessage::getId).orElse(null);

        ChatRoomParticipant participant =
                chatRoomParticipantService.createParticipant(user, chatRoom, lastSeenMessageId);

        eventPublisher.publishEvent(
                new ChatRoomParticipantEnteredEvent(chatRoom, participant, user));

        return ChatRoomEnterResponse.from(participant.getId());
    }

    @Transactional
    public ChatMessageSendCommandResult sendMessageFallback(
            Long userId, Long roomId, ChatMessageSendRequest request) {
        validateSendMessageFallback(userId, roomId, request);

        String idempotencyKey = request.idempotencyKey().trim();

        ChatRoom chatRoom = validateSendMessageAccess(userId, roomId);

        ChatMessageSendCommandResult idempotencyResult =
                resolveIdempotencyState(userId, roomId, idempotencyKey);

        if (idempotencyResult != null) {
            return idempotencyResult;
        }

        List<String> imageKeys = normalizeImageKeys(request.imageKeys());

        List<Image> images = validateSendMessageImages(imageKeys);

        MessageType finalMessageType =
                resolveSendMessageType(request.messageType(), request.content(), imageKeys);

        ChatMessage message =
                chatMessageService.createUserMessage(
                        chatRoom, finalMessageType, request.content(), userId);

        messageImageService.createMessageImages(message, images);

        ChatMessageSendResponse response =
                ChatMessageSendResponse.of(
                        message.getId(),
                        idempotencyKey,
                        "SUCCEEDED",
                        message.getSentAt().atOffset(ZoneOffset.of("+09:00")));

        redisChatMessageIdempotencyStore.markSucceeded(
                userId, roomId, idempotencyKey, message.getId(), response.sentAt());

        return ChatMessageSendCommandResult.succeeded(response);
    }

    @Transactional
    public void updateLastSeenMessage(
            Long userId, Long participantId, UpdateLastReadMessageRequest request) {
        ChatRoomParticipant participant = getOwnedParticipant(userId, participantId);

        chatMessageService.validateMessageInRoom(
                participant.getChatRoom().getId(), request.lastSeenMessageId());

        chatRoomParticipantService.updateLastSeenMessageId(
                participant, request.lastSeenMessageId());

        chatRoomRealtimePublisher.publish(
                "lastSeenUpdated",
                participant.getChatRoom().getId(),
                ChatLastSeenUpdatedResponse.of(
                        UUID.randomUUID().toString(),
                        participant.getChatRoom().getId(),
                        participant.getId(),
                        participant.getUser().getId(),
                        request.lastSeenMessageId()));
    }

    private ChatRoomParticipant getOwnedParticipant(Long userId, Long participantId) {
        if (userId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantService
                        .findById(participantId)
                        .orElseThrow(() -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));

        if (!participant.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        return participant;
    }

    private void validateSendMessageFallback(
            Long userId, Long roomId, ChatMessageSendRequest request) {
        if (userId == null
                || roomId == null
                || request == null
                || request.idempotencyKey() == null
                || request.idempotencyKey().isBlank()
                || request.messageType() == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }
    }

    private ChatRoom validateSendMessageAccess(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);

        chatRoomParticipantService
                .getActiveParticipant(roomId, userId)
                .orElseThrow(() -> new BaseException(ErrorCode.FORBIDDEN_MESSAGE_SEND));

        return chatRoom;
    }

    private ChatMessageSendCommandResult resolveIdempotencyState(
            Long userId, Long roomId, String idempotencyKey) {
        ChatMessageIdempotencyRecord record =
                redisChatMessageIdempotencyStore.find(userId, roomId, idempotencyKey).orElse(null);

        if (record != null) {
            return switch (record.status()) {
                case SUCCEEDED ->
                        ChatMessageSendCommandResult.duplicated(
                                ChatMessageSendResponse.of(
                                        record.messageId(),
                                        idempotencyKey,
                                        "SUCCEEDED",
                                        record.sentAt()));
                case PROCESSING ->
                        ChatMessageSendCommandResult.processing(
                                ChatMessageSendResponse.of(
                                        null, idempotencyKey, "PROCESSING", null));
            };
        }

        if (!redisChatMessageIdempotencyStore.reserve(userId, roomId, idempotencyKey)) {
            return ChatMessageSendCommandResult.processing(
                    ChatMessageSendResponse.of(null, idempotencyKey, "PROCESSING", null));
        }

        return null;
    }

    private List<String> normalizeImageKeys(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return List.of();
        }

        return imageKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .map(String::trim)
                .toList();
    }

    private List<Image> validateSendMessageImages(List<String> imageKeys) {
        if (imageKeys.isEmpty()) {
            return List.of();
        }

        if (imageKeys.stream().distinct().count() != imageKeys.size()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }

        List<Image> images = imageService.getActiveImagesByUploadKeys(imageKeys);

        Map<String, Image> imageMap =
                images.stream().collect(Collectors.toMap(Image::getUploadKey, image -> image));

        if (imageMap.size() != imageKeys.size()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }

        return imageKeys.stream().map(imageMap::get).toList();
    }

    private MessageType resolveSendMessageType(
            MessageType messageType, String content, List<String> imageKeys) {
        boolean hasContent = content != null && !content.isBlank();
        boolean hasImages = !imageKeys.isEmpty();

        return switch (messageType) {
            case TEXT -> {
                if (!hasContent || hasImages) {
                    throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
                }
                yield MessageType.TEXT;
            }
            case TEXT_WITH_IMAGES -> {
                if (!hasContent || !hasImages) {
                    throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
                }
                yield MessageType.TEXT_WITH_IMAGES;
            }
            case IMAGE -> {
                if (!hasImages || hasContent) {
                    throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
                }
                yield MessageType.IMAGE;
            }
            default -> throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        };
    }
}
