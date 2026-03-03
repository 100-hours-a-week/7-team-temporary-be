package molip.server.chat.event;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.MessageImageInfoResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.MessageImage;
import molip.server.chat.service.MessageImageService;
import molip.server.common.enums.ImageType;
import molip.server.common.enums.SenderType;
import molip.server.common.response.ImageInfoResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.socket.dto.response.SocketMessageUpdatedResponse;
import molip.server.user.entity.UserImage;
import molip.server.user.service.UserImageService;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageRealtimePayloadFactory {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MessageImageService messageImageService;
    private final UserService userService;
    private final UserImageService userImageService;
    private final ImageService imageService;

    public ChatMessageCreatedResponse buildMessageCreated(ChatMessage message, List<Image> images) {
        return ChatMessageCreatedResponse.of(
                UUID.randomUUID().toString(),
                message.getId(),
                message.getChatRoom().getId(),
                message.getMessageType(),
                message.getSenderType(),
                message.getSenderId(),
                resolveSenderNickname(message),
                resolveSenderProfile(message),
                message.getContent(),
                toMessageImageResponses(images),
                toKst(message.getSentAt()));
    }

    public SocketMessageUpdatedResponse buildMessageUpdated(ChatMessage message) {
        List<MessageImage> messageImages =
                messageImageService
                        .getMessageImagesByMessageIds(List.of(message.getId()))
                        .getOrDefault(message.getId(), Collections.emptyList());

        return SocketMessageUpdatedResponse.of(
                UUID.randomUUID().toString(),
                message.getId(),
                message.getChatRoom().getId(),
                message.getMessageType(),
                message.getSenderType(),
                message.getSenderId(),
                resolveSenderNickname(message),
                resolveSenderProfile(message),
                message.getContent(),
                toMessageImageResponsesFromEntities(messageImages),
                toKst(resolveEditedAt(message)));
    }

    private String resolveSenderNickname(ChatMessage message) {
        if (message.getSenderType() != SenderType.USER || message.getSenderId() == null) {
            return null;
        }

        return userService.getUser(message.getSenderId()).getNickname();
    }

    private ImageInfoResponse resolveSenderProfile(ChatMessage message) {
        if (message.getSenderType() != SenderType.USER || message.getSenderId() == null) {
            return null;
        }

        return userImageService
                .getLatestUserImage(message.getSenderId())
                .map(UserImage::getImage)
                .map(
                        image ->
                                imageService.issueGetUrlWithoutValidation(
                                        ImageType.USERS, image.getUploadKey()))
                .map(this::toImageInfoResponse)
                .orElse(null);
    }

    private List<MessageImageInfoResponse> toMessageImageResponses(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return java.util.stream.IntStream.range(0, images.size())
                .mapToObj(index -> toMessageImageResponse(images.get(index), index + 1))
                .toList();
    }

    private List<MessageImageInfoResponse> toMessageImageResponsesFromEntities(
            List<MessageImage> messageImages) {
        if (messageImages == null || messageImages.isEmpty()) {
            return List.of();
        }

        return messageImages.stream().map(this::toMessageImageResponse).toList();
    }

    private MessageImageInfoResponse toMessageImageResponse(Image image, int sortOrder) {
        ImageGetUrlResponse imageResponse =
                imageService.issueGetUrlWithoutValidation(ImageType.MESSAGES, image.getUploadKey());

        return MessageImageInfoResponse.of(
                imageResponse.url(), imageResponse.expiresAt(), image.getUploadKey(), sortOrder);
    }

    private MessageImageInfoResponse toMessageImageResponse(MessageImage messageImage) {
        ImageGetUrlResponse imageResponse =
                imageService.issueGetUrlWithoutValidation(
                        ImageType.MESSAGES, messageImage.getImage().getUploadKey());

        return MessageImageInfoResponse.of(
                imageResponse.url(),
                imageResponse.expiresAt(),
                messageImage.getImage().getUploadKey(),
                messageImage.getSortOrder());
    }

    private ImageInfoResponse toImageInfoResponse(ImageGetUrlResponse response) {
        return ImageInfoResponse.of(response.url(), response.expiresAt(), response.imageKey());
    }

    private LocalDateTime resolveEditedAt(ChatMessage message) {
        if (message.getUpdatedAt() != null) {
            return message.getUpdatedAt();
        }

        return LocalDateTime.now();
    }

    private OffsetDateTime toKst(LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, KOREA_ZONE_ID.getRules().getOffset(dateTime));
    }
}
