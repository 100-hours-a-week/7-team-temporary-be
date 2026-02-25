package molip.server.friend.facade;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ImageType;
import molip.server.common.response.ImageInfoResponse;
import molip.server.common.response.PageResponse;
import molip.server.friend.dto.response.FriendRequestItemResponse;
import molip.server.friend.entity.FriendRequest;
import molip.server.friend.service.FriendRequestService;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserImageRepository;
import molip.server.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FriendRequestQueryFacade {
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "default_image.png";
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final FriendRequestService friendRequestService;
    private final UserImageRepository userImageRepository;
    private final ImageService imageService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PageResponse<FriendRequestItemResponse> getFriendRequests(
            Long userId, int page, int size) {

        Page<FriendRequest> friendRequests =
                friendRequestService.getReceivedFriendRequests(userId, page, size);

        Page<FriendRequestItemResponse> mapped = friendRequests.map(this::toResponse);

        return PageResponse.from(mapped, page, size);
    }

    private FriendRequestItemResponse toResponse(FriendRequest friendRequest) {

        Users fromUser = userService.getUser(friendRequest.getFromUserId());

        OffsetDateTime createdAt = friendRequest.getCreatedAt().atZone(ZONE_ID).toOffsetDateTime();

        return FriendRequestItemResponse.of(
                friendRequest.getId(),
                fromUser.getId(),
                fromUser.getEmail(),
                fromUser.getNickname(),
                resolveProfileImage(fromUser.getId()),
                createdAt);
    }

    private ImageInfoResponse resolveProfileImage(Long userId) {

        UserImage userImage = userImageRepository.findLatestByUserIdWithImage(userId).orElse(null);
        if (userImage == null) {
            ImageGetUrlResponse defaultImage =
                    imageService.issueGetUrlWithoutValidation(
                            ImageType.USERS, DEFAULT_PROFILE_IMAGE_KEY);
            return ImageInfoResponse.of(
                    defaultImage.url(), defaultImage.expiresAt(), defaultImage.imageKey());
        }

        Image image = userImage.getImage();
        ImageGetUrlResponse presigned =
                imageService.issueGetUrl(ImageType.USERS, image.getUploadKey());

        return ImageInfoResponse.of(presigned.url(), presigned.expiresAt(), presigned.imageKey());
    }
}
