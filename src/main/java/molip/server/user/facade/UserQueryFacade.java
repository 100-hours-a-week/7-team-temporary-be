package molip.server.user.facade;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ImageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ImageInfoResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.user.dto.response.UserProfileResponse;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserImageRepository;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserQueryFacade {
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        Users user =
                userRepository
                        .findByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        ImageInfoResponse profileImage = resolveProfileImage(userId);
        return new UserProfileResponse(
                user.getEmail(),
                user.getNickname(),
                user.getGender(),
                user.getBirth().toString(),
                user.getFocusTimeZone(),
                user.getDayEndTime().toString(),
                profileImage);
    }

    private ImageInfoResponse resolveProfileImage(Long userId) {
        Optional<UserImage> userImage = userImageRepository.findLatestByUserIdWithImage(userId);
        if (userImage.isEmpty()) {
            return null;
        }
        Image image = userImage.get().getImage();
        ImageGetUrlResponse presigned =
                imageService.issueGetUrl(ImageType.USERS, image.getUploadKey());
        return new ImageInfoResponse(presigned.url(), presigned.expiresAt(), presigned.imageKey());
    }
}
