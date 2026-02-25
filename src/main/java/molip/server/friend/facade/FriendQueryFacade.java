package molip.server.friend.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ImageType;
import molip.server.common.response.ImageInfoResponse;
import molip.server.common.response.PageResponse;
import molip.server.friend.dto.response.FriendItemResponse;
import molip.server.friend.entity.Friend;
import molip.server.friend.service.FriendService;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.service.UserImageService;
import molip.server.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FriendQueryFacade {
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "default_image.png";

    private final FriendService friendService;
    private final UserService userService;
    private final UserImageService userImageService;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public PageResponse<FriendItemResponse> getFriends(Long userId, int page, int size) {
        Page<Friend> friends = friendService.getFriends(userId, page, size);

        Page<FriendItemResponse> mapped = friends.map(this::toResponse);

        return PageResponse.from(mapped, page, size);
    }

    private FriendItemResponse toResponse(Friend friend) {
        Long friendUserId = friend.getFriendId();
        Users friendUser = userService.getUser(friendUserId);

        return FriendItemResponse.of(
                friendUserId,
                friendUser.getEmail(),
                friendUser.getNickname(),
                resolveProfileImage(friendUserId));
    }

    private ImageInfoResponse resolveProfileImage(Long userId) {

        UserImage userImage = userImageService.getLatestUserImage(userId).orElse(null);

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
