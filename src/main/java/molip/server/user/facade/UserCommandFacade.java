package molip.server.user.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.UploadStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.entity.Image;
import molip.server.image.repository.ImageRepository;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserImageRepository;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserCommandFacade {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserImageRepository userImageRepository;

    @Transactional
    public void linkProfileImage(Long userId, String imageKey) {
        Users user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Image image =
                imageRepository
                        .findByUploadKeyAndUploadStatusAndDeletedAtIsNull(
                                imageKey, UploadStatus.PENDING)
                        .orElseThrow(() -> new BaseException(ErrorCode.CONFLICT_INVALID_IMAGE_KEY));

        image.markSuccess();
        userImageRepository.save(new UserImage(user, image));
    }

    @Transactional
    public void changeProfileImage(Long userId, String imageKey) {
        Users user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Image newImage =
                imageRepository
                        .findByUploadKeyAndUploadStatusAndDeletedAtIsNull(
                                imageKey, UploadStatus.PENDING)
                        .orElseThrow(() -> new BaseException(ErrorCode.CONFLICT_INVALID_IMAGE_KEY));

        userImageRepository
                .findLatestByUserIdWithImage(userId)
                .ifPresent(
                        userImage -> {
                            userImage.deleteUserImage();
                            userImage.getImage().deleteImage();
                        });

        newImage.markSuccess();
        userImageRepository.save(new UserImage(user, newImage));
    }
}
