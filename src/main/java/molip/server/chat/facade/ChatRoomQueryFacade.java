package molip.server.chat.facade;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMessageItemResponse;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.dto.response.ChatRoomDetailResponse;
import molip.server.chat.dto.response.ChatRoomOwnerCheckResponse;
import molip.server.chat.dto.response.ChatRoomOwnerResponse;
import molip.server.chat.dto.response.ChatRoomParticipantResponse;
import molip.server.chat.dto.response.ChatRoomSearchItemResponse;
import molip.server.chat.dto.response.MessageImageInfoResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.entity.MessageImage;
import molip.server.chat.event.ChatLastSeenReadSyncedEvent;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.chat.service.MessageImageService;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.enums.ImageType;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.CursorResponse;
import molip.server.common.response.ImageInfoResponse;
import molip.server.common.response.PageResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.service.ImageService;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.service.UserImageService;
import molip.server.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatRoomQueryFacade {

    private static final String DEFAULT_PROFILE_IMAGE_KEY = "user_default.svg";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final MessageImageService messageImageService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserImageService userImageService;
    private final UserService userService;
    private final ImageService imageService;

    public ChatRoomDetailResponse getChatRoomDetail(Long roomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);

        List<ChatRoomParticipant> participants =
                chatRoomParticipantService.getActiveParticipants(roomId);

        ChatRoomParticipant ownerParticipant =
                chatRoomParticipantService
                        .getActiveParticipant(roomId, chatRoom.getOwnerId())
                        .orElseThrow(() -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));

        ChatRoomOwnerResponse owner = buildOwner(ownerParticipant);

        List<ChatRoomParticipantResponse> participantResponses =
                participants.stream()
                        .filter(
                                participant ->
                                        !participant
                                                .getUser()
                                                .getId()
                                                .equals(chatRoom.getOwnerId()))
                        .map(this::buildParticipant)
                        .toList();

        return ChatRoomDetailResponse.of(
                chatRoom.getId(),
                chatRoom.getType(),
                chatRoom.getTitle(),
                chatRoom.getDescription(),
                chatRoom.getMaxParticipants(),
                owner,
                participantResponses,
                participants.size());
    }

    public PageResponse<ChatRoomSearchItemResponse> searchChatRooms(
            Long userId, String title, ChatRoomType type, int page, int size) {

        Page<ChatRoom> chatRoomPage = chatRoomService.searchChatRooms(title, type, page, size);

        List<Long> chatRoomIds = chatRoomPage.getContent().stream().map(ChatRoom::getId).toList();

        Map<Long, Integer> participantsCountMap =
                chatRoomParticipantService.countActiveParticipantsByChatRoomIds(chatRoomIds);

        Set<Long> joinedRoomIds =
                chatRoomParticipantService.findJoinedChatRoomIds(userId, chatRoomIds);

        List<ChatRoomSearchItemResponse> content =
                chatRoomPage.getContent().stream()
                        .map(
                                chatRoom ->
                                        ChatRoomSearchItemResponse.of(
                                                chatRoom,
                                                participantsCountMap.getOrDefault(
                                                        chatRoom.getId(), 0),
                                                joinedRoomIds.contains(chatRoom.getId())))
                        .toList();

        return PageResponse.of(chatRoomPage, content, page, size);
    }

    public PageResponse<ChatMyRoomItemResponse> getMyChatRooms(
            Long userId, ChatRoomType type, int page, int size) {
        Page<ChatRoomParticipant> participationPage =
                chatRoomParticipantService.getMyActiveParticipations(userId, type, page, size);

        List<Long> chatRoomIds =
                participationPage.getContent().stream()
                        .map(participant -> participant.getChatRoom().getId())
                        .toList();

        Map<Long, Integer> participantsCountMap =
                chatRoomParticipantService.countActiveParticipantsByChatRoomIds(chatRoomIds);

        List<ChatMyRoomItemResponse> content =
                participationPage.getContent().stream()
                        .map(
                                participation -> {
                                    ChatRoom chatRoom = participation.getChatRoom();
                                    ChatMessage latestMessage =
                                            chatMessageService
                                                    .getLatestNonSystemMessage(chatRoom.getId())
                                                    .orElse(null);
                                    int participantsCount =
                                            participantsCountMap.getOrDefault(chatRoom.getId(), 0);

                                    return buildMyChatRoomItem(
                                            participation, latestMessage, participantsCount);
                                })
                        .toList();

        return PageResponse.of(participationPage, content, page, size);
    }

    @Transactional
    public CursorResponse<ChatMessageItemResponse> getMessages(
            Long userId, Long roomId, Long cursor, int size) {
        ChatRoomParticipant participant = validateChatMessageAccess(userId, roomId);

        Page<ChatMessage> messagePage = chatMessageService.getMessages(roomId, cursor, size);

        List<ChatMessage> messages = messagePage.getContent();

        if (cursor == null && !messages.isEmpty()) {
            Long latestMessageId = messages.getFirst().getId();

            if (participant.getLastSeenMessageId() == null
                    || latestMessageId > participant.getLastSeenMessageId()) {
                eventPublisher.publishEvent(
                        new ChatLastSeenReadSyncedEvent(
                                participant.getId(), userId, roomId, latestMessageId));
            }
        }

        List<Long> messageIds = messages.stream().map(ChatMessage::getId).toList();

        Map<Long, List<MessageImage>> messageImageMap =
                messageImageService.getMessageImagesByMessageIds(messageIds);

        List<ChatMessageItemResponse> content =
                messages.stream()
                        .map(
                                message ->
                                        buildChatMessageItem(
                                                message,
                                                messageImageMap.getOrDefault(
                                                        message.getId(), List.of())))
                        .toList();

        boolean hasNext = messagePage.hasNext();

        Long nextCursor = hasNext && !messages.isEmpty() ? messages.getLast().getId() : null;

        return new CursorResponse<>(content, nextCursor, hasNext, size);
    }

    public ChatRoomOwnerCheckResponse checkOwner(Long roomId, Long ownerId) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);

        if (!userService.existsUser(ownerId)) {
            throw new BaseException(ErrorCode.OWNER_NOT_FOUND);
        }

        return ChatRoomOwnerCheckResponse.from(chatRoom.getOwnerId().equals(ownerId));
    }

    public ChatMyRoomItemResponse buildMyChatRoomItem(
            ChatRoomParticipant participation, ChatMessage latestMessage, int participantsCount) {
        ChatRoom chatRoom = participation.getChatRoom();
        int unreadCount =
                chatMessageService.countUnreadMessages(
                        chatRoom.getId(), participation.getLastSeenMessageId());

        return ChatMyRoomItemResponse.of(
                chatRoom,
                participantsCount,
                unreadCount,
                resolveLastUserMessagePreview(latestMessage),
                latestMessage != null ? toKst(latestMessage.getSentAt()) : null);
    }

    private ChatRoomOwnerResponse buildOwner(ChatRoomParticipant ownerParticipant) {
        Users owner = ownerParticipant.getUser();

        return ChatRoomOwnerResponse.of(
                owner.getId(),
                owner.getNickname(),
                ownerParticipant.getId(),
                ownerParticipant.isCameraEnabled(),
                getProfileImage(owner.getId()));
    }

    private ChatRoomParticipantResponse buildParticipant(ChatRoomParticipant participant) {
        Users user = participant.getUser();

        return ChatRoomParticipantResponse.of(
                participant.getId(),
                user.getId(),
                user.getNickname(),
                participant.isCameraEnabled(),
                getProfileImage(user.getId()),
                toKst(participant.getCreatedAt()));
    }

    private ChatMessageItemResponse buildChatMessageItem(
            ChatMessage message, List<MessageImage> messageImages) {

        List<MessageImageInfoResponse> images =
                messageImages.stream().map(this::toMessageImageInfoResponse).toList();

        return ChatMessageItemResponse.of(
                message.getId(),
                message.getMessageType(),
                message.getSenderType(),
                message.getSenderId(),
                getSenderNickname(message),
                getSenderProfileImage(message),
                message.getContent(),
                images,
                toKst(message.getSentAt()));
    }

    private ChatRoomParticipant validateChatMessageAccess(Long userId, Long roomId) {
        chatRoomService.getChatRoom(roomId);

        return chatRoomParticipantService
                .getActiveParticipant(roomId, userId)
                .orElseThrow(() -> new BaseException(ErrorCode.FORBIDDEN_CHAT_ACCESS));
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

    private MessageImageInfoResponse toMessageImageInfoResponse(MessageImage messageImage) {
        ImageGetUrlResponse response =
                imageService.issueGetUrlWithoutValidation(
                        ImageType.MESSAGES, messageImage.getImage().getUploadKey());

        return MessageImageInfoResponse.of(
                response.url(),
                response.expiresAt(),
                response.imageKey(),
                messageImage.getSortOrder());
    }

    private OffsetDateTime toKst(java.time.LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }

    private String resolveLastUserMessagePreview(ChatMessage latestMessage) {
        if (latestMessage == null) {
            return null;
        }

        if (latestMessage.getMessageType() == MessageType.IMAGE) {
            return "이미지를 보냈습니다";
        }

        return latestMessage.getContent();
    }

    private String getSenderNickname(ChatMessage message) {
        if (message.getSenderType() != molip.server.common.enums.SenderType.USER
                || message.getSenderId() == null) {
            return null;
        }

        return userService.getUser(message.getSenderId()).getNickname();
    }

    private ImageInfoResponse getSenderProfileImage(ChatMessage message) {
        if (message.getSenderType() != SenderType.USER || message.getSenderId() == null) {
            return null;
        }

        return getProfileImage(message.getSenderId());
    }
}
