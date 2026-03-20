package molip.server.chat.facade;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.ChatMessageSendRequest;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.dto.response.ChatDirectRoomEnterResponse;
import molip.server.chat.dto.response.ChatLastSeenUpdatedResponse;
import molip.server.chat.dto.response.ChatMessageSendCommandResult;
import molip.server.chat.dto.response.ChatMessageSendResponse;
import molip.server.chat.dto.response.ChatParticipantJoinedResponse;
import molip.server.chat.dto.response.ChatRoomEnterResponse;
import molip.server.chat.dto.response.VideoCameraChangedResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatMessageDeletedEvent;
import molip.server.chat.event.ChatMessageSentEvent;
import molip.server.chat.event.ChatMessageUpdatedEvent;
import molip.server.chat.event.ChatRoomParticipantEnteredEvent;
import molip.server.chat.event.VideoCameraChangedEvent;
import molip.server.chat.event.VideoRoomParticipantEnteredEvent;
import molip.server.chat.redis.idempotency.ChatMessageIdempotencyRecord;
import molip.server.chat.redis.idempotency.RedisChatMessageIdempotencyStore;
import molip.server.chat.redis.presence.RedisVideoParticipantPresenceStore;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.chat.service.MessageImageService;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.enums.ImageType;
import molip.server.common.enums.MessageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ImageInfoResponse;
import molip.server.friend.service.FriendService;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.service.UserImageService;
import molip.server.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatRoomCommandFacade {

    private static final String DEFAULT_PROFILE_IMAGE_KEY = "user_default.svg";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final MessageImageService messageImageService;
    private final FriendService friendService;
    private final UserService userService;
    private final UserImageService userImageService;
    private final ImageService imageService;
    private final RedisChatMessageIdempotencyStore redisChatMessageIdempotencyStore;
    private final RedisVideoParticipantPresenceStore redisVideoParticipantPresenceStore;
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

        if (chatRoom.getType() == ChatRoomType.CAM_STUDY) {
            eventPublisher.publishEvent(
                    new VideoRoomParticipantEnteredEvent(
                            chatRoom.getId(),
                            ChatParticipantJoinedResponse.of(
                                    UUID.randomUUID().toString(),
                                    chatRoom.getId(),
                                    participant.getId(),
                                    user.getId(),
                                    user.getNickname(),
                                    participant.isCameraEnabled(),
                                    toKst(participant.getCreatedAt()))));
        } else {
            eventPublisher.publishEvent(
                    new ChatRoomParticipantEnteredEvent(chatRoom, participant, user));
        }

        return ChatRoomEnterResponse.from(participant.getId());
    }

    private OffsetDateTime toKst(LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }

    @Transactional
    public ChatDirectRoomEnterResponse enterOrCreateDirectChatRoom(Long userId, Long friendId) {
        validateDirectChatRequest(userId, friendId);
        Users user = userService.getUser(userId);
        Users friend = getFriendUser(friendId);

        friendService.lockFriendRelationPair(userId, friendId);

        ChatRoom directRoom = findOrCreateDirectRoom(userId, friendId);
        ensureActiveDirectParticipant(user, directRoom);
        ensureActiveDirectParticipant(friend, directRoom);

        return ChatDirectRoomEnterResponse.of(directRoom.getId(), "JOINED");
    }

    @Transactional
    public void updateParticipantCamera(Long userId, Long participantId, Boolean cameraEnabled) {
        updateParticipantCamera(userId, null, participantId, cameraEnabled);
    }

    @Transactional
    public void updateParticipantCamera(
            Long userId, Long roomId, Long participantId, Boolean cameraEnabled) {
        if (userId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (cameraEnabled == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_CAMERA_REQUIRED);
        }

        ChatRoomParticipant participant =
                roomId == null
                        ? chatRoomParticipantService.getActiveParticipantById(participantId)
                        : chatRoomParticipantService.getActiveParticipantByIdAndRoomId(
                                participantId, roomId);

        if (!participant.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_CAMERA_UPDATE);
        }

        if (participant.isCameraEnabled() == cameraEnabled) {
            return;
        }

        participant.updateCameraEnabled(cameraEnabled);
        redisVideoParticipantPresenceStore.updateCameraEnabledByParticipant(
                participant.getChatRoom().getId(), participant.getId(), cameraEnabled);

        eventPublisher.publishEvent(
                new VideoCameraChangedEvent(
                        participant.getChatRoom().getId(),
                        VideoCameraChangedResponse.of(
                                UUID.randomUUID().toString(),
                                participant.getChatRoom().getId(),
                                participant.getId(),
                                participant.getUser().getId(),
                                cameraEnabled,
                                OffsetDateTime.now(ZoneOffset.of("+09:00")))));
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

        chatRoomParticipantService
                .getActiveParticipant(roomId, userId)
                .ifPresent(
                        participant ->
                                chatRoomParticipantService.updateLastSeenMessageId(
                                        participant, message.getId()));

        messageImageService.createMessageImages(message, images);

        Users user = userService.getUser(userId);

        ChatMessageSendResponse response =
                ChatMessageSendResponse.of(
                        message.getId(),
                        idempotencyKey,
                        "SUCCEEDED",
                        user.getNickname(),
                        getProfileImage(userId),
                        message.getSentAt().atOffset(ZoneOffset.of("+09:00")));

        redisChatMessageIdempotencyStore.markSucceeded(
                userId, roomId, idempotencyKey, message.getId(), response.sentAt());

        eventPublisher.publishEvent(new ChatMessageSentEvent(chatRoom, message, images, userId));

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

    @Transactional
    public void updateMessage(Long userId, Long roomId, Long messageId, String content) {
        validateUpdateMessageRequest(userId, roomId, messageId, content);

        validateSendMessageAccess(userId, roomId);

        ChatMessage message = chatMessageService.updateMessage(userId, roomId, messageId, content);

        eventPublisher.publishEvent(new ChatMessageUpdatedEvent(message));
    }

    @Transactional
    public void deleteMessage(Long userId, Long roomId, Long messageId) {
        validateDeleteMessageRequest(userId, roomId, messageId);

        validateSendMessageAccess(userId, roomId);

        ChatMessage message = chatMessageService.deleteMessage(userId, roomId, messageId);

        eventPublisher.publishEvent(new ChatMessageDeletedEvent(message));
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

    private void validateUpdateMessageRequest(
            Long userId, Long roomId, Long messageId, String content) {

        if (userId == null
                || roomId == null
                || messageId == null
                || content == null
                || content.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
        }
    }

    private void validateDeleteMessageRequest(Long userId, Long roomId, Long messageId) {
        if (userId == null || roomId == null || messageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_DELETE);
        }
    }

    private void validateDirectChatRequest(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (userId.equals(friendId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_SELF_FRIEND);
        }
    }

    private Users getFriendUser(Long friendId) {
        try {
            return userService.getUser(friendId);
        } catch (BaseException exception) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND_TARGET);
        }
    }

    private ChatRoom findOrCreateDirectRoom(Long userId, Long friendId) {
        ChatRoom directRoom = chatRoomService.findDirectRoomByUserPair(userId, friendId);

        if (directRoom != null) {
            return directRoom;
        }

        return chatRoomService.createDirectChatRoom(userId, friendId);
    }

    private ChatRoomParticipant ensureActiveDirectParticipant(Users user, ChatRoom directRoom) {
        ChatRoomParticipant participant =
                chatRoomParticipantService
                        .getActiveParticipant(directRoom.getId(), user.getId())
                        .orElse(null);

        if (participant == null) {
            return chatRoomParticipantService.createParticipant(
                    user, directRoom, getLatestMessageId(directRoom.getId()));
        }

        return participant;
    }

    private Long getLatestMessageId(Long roomId) {
        return chatMessageService.getLatestMessage(roomId).map(ChatMessage::getId).orElse(null);
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
                                        null,
                                        null,
                                        record.sentAt()));
                case PROCESSING ->
                        ChatMessageSendCommandResult.processing(
                                ChatMessageSendResponse.of(
                                        null, idempotencyKey, "PROCESSING", null, null, null));
            };
        }

        if (!redisChatMessageIdempotencyStore.reserve(userId, roomId, idempotencyKey)) {
            return ChatMessageSendCommandResult.processing(
                    ChatMessageSendResponse.of(
                            null, idempotencyKey, "PROCESSING", null, null, null));
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

        List<Image> images = imageService.getPendingImagesByUploadKeys(imageKeys);

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

    private ImageInfoResponse getProfileImage(Long userId) {
        return userImageService
                .getLatestUserImage(userId)
                .map(UserImage::getImage)
                .map(
                        image ->
                                imageService.issueGetUrlWithoutValidation(
                                        ImageType.USERS, image.getUploadKey()))
                .map(this::toImageInfoResponse)
                .orElseGet(this::getDefaultProfileImage);
    }

    private ImageInfoResponse getDefaultProfileImage() {
        ImageGetUrlResponse response =
                imageService.issueGetUrlWithoutValidation(
                        ImageType.USERS, DEFAULT_PROFILE_IMAGE_KEY);

        return ImageInfoResponse.of(response.url(), response.expiresAt(), response.imageKey());
    }

    private ImageInfoResponse toImageInfoResponse(ImageGetUrlResponse response) {
        return ImageInfoResponse.of(response.url(), response.expiresAt(), response.imageKey());
    }
}
