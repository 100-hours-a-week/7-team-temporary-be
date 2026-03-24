package molip.server.user.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.cache.ReadConsistencyCacheService;
import molip.server.common.enums.UploadStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.entity.Image;
import molip.server.image.repository.ImageRepository;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.outbox.core.service.OutboxEventService;
import molip.server.user.dto.cache.UserCachePayload;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;
import molip.server.user.event.UserProfileImageDeletedEvent;
import molip.server.user.repository.UserImageRepository;
import molip.server.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserCommandFacade {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserImageRepository userImageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxEventService outboxEventService;
    private final ReadConsistencyCacheService cacheService;

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
        outboxEventService.recordUpdated(
                AggregateType.IMAGE, image.getId(), OutboxPayloadMapper.image(image));

        UserImage userImage = userImageRepository.save(new UserImage(user, image));
        outboxEventService.recordCreated(
                AggregateType.USER_IMAGE,
                userImage.getId(),
                OutboxPayloadMapper.userImage(userImage));
        long cacheVersion =
                Math.max(resolveVersion(user.getVersion()), resolveVersion(userImage.getVersion()));
        cacheService.cacheUser(UserCachePayload.from(user, imageKey, cacheVersion));
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
                            Image oldImage = userImage.getImage();
                            oldImage.deleteImage();
                            eventPublisher.publishEvent(
                                    new UserProfileImageDeletedEvent(
                                            oldImage.getImageType(), oldImage.getUploadKey()));
                            outboxEventService.recordDeleted(
                                    AggregateType.USER_IMAGE,
                                    userImage.getId(),
                                    OutboxPayloadMapper.userImage(userImage));
                            outboxEventService.recordDeleted(
                                    AggregateType.IMAGE,
                                    oldImage.getId(),
                                    OutboxPayloadMapper.image(oldImage));
                        });

        newImage.markSuccess();
        outboxEventService.recordUpdated(
                AggregateType.IMAGE, newImage.getId(), OutboxPayloadMapper.image(newImage));

        UserImage savedUserImage = userImageRepository.save(new UserImage(user, newImage));
        outboxEventService.recordCreated(
                AggregateType.USER_IMAGE,
                savedUserImage.getId(),
                OutboxPayloadMapper.userImage(savedUserImage));
        long cacheVersion =
                Math.max(
                        resolveVersion(user.getVersion()),
                        resolveVersion(savedUserImage.getVersion()));
        cacheService.cacheUser(UserCachePayload.from(user, imageKey, cacheVersion));
    }

    private long resolveVersion(Long version) {
        return version == null ? 0L : Math.max(0L, version);
    }
}
