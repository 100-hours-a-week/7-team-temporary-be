package molip.server.chat.facade;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.dto.response.ChatRoomDetailResponse;
import molip.server.chat.dto.response.ChatRoomOwnerResponse;
import molip.server.chat.dto.response.ChatRoomParticipantResponse;
import molip.server.chat.dto.response.ChatRoomSearchItemResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.service.ChatMessageService;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.enums.ImageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ImageInfoResponse;
import molip.server.common.response.PageResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.service.ImageService;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.service.UserImageService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomQueryFacade {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final UserImageService userImageService;
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
            String title, int page, int size) {

        Page<ChatRoom> chatRoomPage = chatRoomService.searchChatRooms(title, page, size);

        List<Long> chatRoomIds = chatRoomPage.getContent().stream().map(ChatRoom::getId).toList();

        Map<Long, Integer> participantsCountMap =
                chatRoomParticipantService.countActiveParticipantsByChatRoomIds(chatRoomIds);

        List<ChatRoomSearchItemResponse> content =
                chatRoomPage.getContent().stream()
                        .map(
                                chatRoom ->
                                        ChatRoomSearchItemResponse.of(
                                                chatRoom,
                                                participantsCountMap.getOrDefault(
                                                        chatRoom.getId(), 0)))
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
                                                    .getLatestMessage(chatRoom.getId())
                                                    .orElse(null);
                                    int participantsCount =
                                            participantsCountMap.getOrDefault(chatRoom.getId(), 0);

                                    return buildMyChatRoomItem(
                                            participation, latestMessage, participantsCount);
                                })
                        .toList();

        return PageResponse.of(participationPage, content, page, size);
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
                latestMessage != null ? latestMessage.getContent() : null,
                latestMessage != null ? toKst(latestMessage.getSentAt()) : null);
    }

    private ChatRoomOwnerResponse buildOwner(ChatRoomParticipant ownerParticipant) {
        Users owner = ownerParticipant.getUser();

        return ChatRoomOwnerResponse.of(
                owner.getId(),
                owner.getNickname(),
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

    private ImageInfoResponse getProfileImage(Long userId) {
        return userImageService
                .getLatestUserImage(userId)
                .map(UserImage::getImage)
                .map(
                        image ->
                                imageService.issueGetUrlWithoutValidation(
                                        ImageType.USERS, image.getUploadKey()))
                .map(this::toImageInfoResponse)
                .orElse(null);
    }

    private ImageInfoResponse toImageInfoResponse(ImageGetUrlResponse response) {
        return ImageInfoResponse.of(response.url(), response.expiresAt(), response.imageKey());
    }

    private OffsetDateTime toKst(java.time.LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
