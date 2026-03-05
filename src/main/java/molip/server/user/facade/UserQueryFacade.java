package molip.server.user.facade;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.common.cache.ReadConsistencyCacheService;
import molip.server.common.enums.ImageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ImageInfoResponse;
import molip.server.common.response.PageResponse;
import molip.server.friend.service.FriendRequestService;
import molip.server.friend.service.FriendService;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.user.dto.cache.UserCachePayload;
import molip.server.user.dto.response.FriendRelationStatus;
import molip.server.user.dto.response.UserProfileResponse;
import molip.server.user.dto.response.UserSearchItemResponse;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserImageRepository;
import molip.server.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserQueryFacade {
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "user_default.svg";

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final FriendService friendService;
    private final FriendRequestService friendRequestService;
    private final ImageService imageService;
    private final ReadConsistencyCacheService cacheService;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        Optional<Users> user = userRepository.findByIdAndDeletedAtIsNull(userId);
        if (user.isPresent()) {
            ImageInfoResponse profileImage = resolveProfileImage(userId);
            return new UserProfileResponse(
                    user.get().getEmail(),
                    user.get().getNickname(),
                    user.get().getGender(),
                    user.get().getBirth().toString(),
                    user.get().getFocusTimeZone(),
                    user.get().getDayEndTime().toString(),
                    profileImage);
        }

        UserCachePayload cached =
                cacheService
                        .getUser(userId)
                        .filter(payload -> payload.deletedAt() == null)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        ImageInfoResponse profileImage = resolveProfileImageFromCache(cached.profileImageKey());
        return new UserProfileResponse(
                cached.email(),
                cached.nickname(),
                cached.gender(),
                cached.birth(),
                cached.focusTimeZone(),
                cached.dayEndTime(),
                profileImage);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSearchItemResponse> searchByNickname(
            Long userId, String nickname, int page, int size) {
        validateNickname(nickname);
        validatePage(page, size);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Users> searchedUsers = userRepository.findByNicknamePrefix(nickname, pageRequest);

        List<Long> searchedUserIds = searchedUsers.getContent().stream().map(Users::getId).toList();

        Set<Long> friendUserIds = friendService.getFriendUserIds(userId, searchedUserIds);

        Set<Long> pendingUserIds =
                friendRequestService.getPendingRelationUserIds(userId, searchedUserIds);

        return new PageResponse<>(
                searchedUsers.getContent().stream()
                        .map(
                                user ->
                                        UserSearchItemResponse.of(
                                                user.getId(),
                                                user.getNickname(),
                                                user.getEmail(),
                                                resolveProfileImage(user.getId()),
                                                resolveRelationStatus(
                                                        userId,
                                                        user.getId(),
                                                        friendUserIds,
                                                        pendingUserIds)))
                        .toList(),
                page,
                size,
                searchedUsers.getTotalElements(),
                searchedUsers.getTotalPages());
    }

    private FriendRelationStatus resolveRelationStatus(
            Long userId, Long targetUserId, Set<Long> friendUserIds, Set<Long> pendingUserIds) {

        if (userId.equals(targetUserId)) {
            return FriendRelationStatus.SELF;
        }

        if (friendUserIds.contains(targetUserId)) {
            return FriendRelationStatus.FRIEND;
        }

        if (pendingUserIds.contains(targetUserId)) {
            return FriendRelationStatus.PENDING;
        }

        return FriendRelationStatus.NONE;
    }

    private ImageInfoResponse resolveProfileImage(Long userId) {
        Optional<UserImage> userImage = userImageRepository.findLatestByUserIdWithImage(userId);

        if (userImage.isEmpty()) {
            return resolveProfileImageFromCache(null);
        }

        Image image = userImage.get().getImage();
        ImageGetUrlResponse presigned =
                imageService.issueGetUrl(ImageType.USERS, image.getUploadKey());

        return new ImageInfoResponse(presigned.url(), presigned.expiresAt(), presigned.imageKey());
    }

    private ImageInfoResponse resolveProfileImageFromCache(String profileImageKey) {
        if (profileImageKey == null || profileImageKey.isBlank()) {
            ImageGetUrlResponse presigned =
                    imageService.issueGetUrlWithoutValidation(
                            ImageType.USERS, DEFAULT_PROFILE_IMAGE_KEY);

            return new ImageInfoResponse(
                    presigned.url(), presigned.expiresAt(), presigned.imageKey());
        }

        ImageGetUrlResponse presigned = imageService.issueGetUrl(ImageType.USERS, profileImageKey);

        return new ImageInfoResponse(presigned.url(), presigned.expiresAt(), presigned.imageKey());
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_NICKNAME_REQUIRED);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }
}
