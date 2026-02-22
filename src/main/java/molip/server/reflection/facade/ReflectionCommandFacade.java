package molip.server.reflection.facade;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.UploadStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.entity.Image;
import molip.server.image.repository.ImageRepository;
import molip.server.reflection.dto.request.ReflectionCreateRequest;
import molip.server.reflection.dto.response.ReflectionCreateResponse;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.event.ReflectionImagesAddedEvent;
import molip.server.reflection.event.ReflectionImagesRemovedEvent;
import molip.server.reflection.repository.DayReflectionImageRepository;
import molip.server.reflection.repository.DayReflectionRepository;
import molip.server.reflection.service.ReflectionService;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.repository.DayPlanRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReflectionCommandFacade {

    private final DayPlanRepository dayPlanRepository;
    private final DayReflectionRepository dayReflectionRepository;
    private final DayReflectionImageRepository dayReflectionImageRepository;
    private final ImageRepository imageRepository;
    private final ReflectionService reflectionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReflectionCreateResponse createReflection(
            Long userId, Long dayPlanId, ReflectionCreateRequest request) {

        List<String> imageKeys = request.reflectionImageKeys();

        DayPlan dayPlan =
                dayPlanRepository
                        .findByIdAndDeletedAtIsNull(dayPlanId)
                        .orElseThrow(
                                () -> new BaseException(ErrorCode.DAYPLAN_NOT_FOUND_REFLECTION));

        validateOwnership(dayPlan, userId);
        validateNotExists(dayPlanId);

        List<Image> images = resolveImagesForCreate(imageKeys);

        String content = request.content() == null ? "" : request.content();
        boolean isOpen = request.isPublic() != null && request.isPublic();

        DayReflection reflection =
                reflectionService.createReflection(dayPlan, content, isOpen, images);

        return ReflectionCreateResponse.from(reflection.getId());
    }

    private void validateOwnership(DayPlan dayPlan, Long userId) {
        if (!dayPlan.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REFLECTION_ONLY_OWN);
        }
    }

    private void validateNotExists(Long dayPlanId) {
        if (dayReflectionRepository.existsByDayPlanIdAndDeletedAtIsNull(dayPlanId)) {
            throw new BaseException(ErrorCode.CONFLICT_REFLECTION_ALREADY_EXISTS);
        }
    }

    private List<Image> resolveImagesForCreate(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return List.of();
        }

        List<String> uniqueImageKeys = uniqueKeys(imageKeys);
        List<Image> images = imageRepository.findByUploadKeyInAndDeletedAtIsNull(uniqueImageKeys);

        if (images.size() != uniqueImageKeys.size()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_IMAGES);
        }

        validatePendingOnly(images);
        return images;
    }

    private List<String> uniqueKeys(List<String> imageKeys) {
        validateImageKeys(imageKeys);
        Set<String> unique = new LinkedHashSet<>(imageKeys);

        return new ArrayList<>(unique);
    }

    private void validateImageKeys(List<String> imageKeys) {
        for (String imageKey : imageKeys) {
            if (imageKey == null || imageKey.isBlank()) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_IMAGES);
            }
        }
    }

    private void validatePendingOnly(List<Image> images) {
        for (Image image : images) {
            if (image.getUploadStatus() != UploadStatus.PENDING) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_IMAGES);
            }
        }
    }

    private void validateNewImagesPending(List<Image> images, Set<Long> existingImageIds) {
        for (Image image : images) {
            if (existingImageIds.contains(image.getId())) {
                continue;
            }
            if (image.getUploadStatus() != UploadStatus.PENDING) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_IMAGES);
            }
        }
    }

    @Transactional
    public void updateReflection(
            Long userId, Long reflectionId, List<String> reflectionImageKeys, String content) {

        List<String> imageKeys = reflectionImageKeys == null ? List.of() : reflectionImageKeys;
        validateUpdateReflection(userId, reflectionId);

        DayReflection reflection = reflectionService.getReflection(reflectionId);
        validateReflectionOwnership(reflection, userId);

        if (content != null) {
            reflection.updateContent(content);
        }

        List<DayReflectionImage> existing =
                dayReflectionImageRepository.findByReflectionIdWithImage(reflectionId);

        Set<Long> existingImageIds =
                existing.stream().map(item -> item.getImage().getId()).collect(Collectors.toSet());

        List<String> uniqueImageKeys = uniqueKeys(imageKeys);
        List<Image> images = imageRepository.findByUploadKeyInAndDeletedAtIsNull(uniqueImageKeys);

        if (images.size() != uniqueImageKeys.size()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_IMAGES);
        }

        validateNewImagesPending(images, existingImageIds);

        Set<Long> incomingImageIds = images.stream().map(Image::getId).collect(Collectors.toSet());

        List<Long> removeImageIds =
                existingImageIds.stream().filter(id -> !incomingImageIds.contains(id)).toList();

        List<Image> addImages =
                images.stream().filter(image -> !existingImageIds.contains(image.getId())).toList();

        publishImageUpdateEvents(reflection.getId(), removeImageIds, addImages, reflection);
    }

    @Transactional
    public void deleteReflection(Long userId, Long reflectionId) {
        validateDeleteReflection(userId, reflectionId);

        DayReflection reflection =
                dayReflectionRepository
                        .findById(reflectionId)
                        .orElseThrow(() -> new BaseException(ErrorCode.REFLECTION_NOT_FOUND));

        if (reflection.getDeletedAt() != null) {
            throw new BaseException(ErrorCode.REFLECTION_ALREADY_DELETED);
        }

        validateReflectionOwnership(reflection, userId);

        List<DayReflectionImage> existing =
                dayReflectionImageRepository.findByReflectionIdWithImage(reflectionId);

        List<Long> removeImageIds = existing.stream().map(item -> item.getImage().getId()).toList();

        if (!removeImageIds.isEmpty()) {
            eventPublisher.publishEvent(
                    new ReflectionImagesRemovedEvent(reflectionId, removeImageIds));
        }

        reflection.delete();
    }

    private void validateReflectionOwnership(DayReflection reflection, Long userId) {
        if (!reflection.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REFLECTION_UPDATE);
        }
    }

    private void validateUpdateReflection(Long userId, Long reflectionId) {
        if (userId == null || reflectionId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void validateDeleteReflection(Long userId, Long reflectionId) {
        if (userId == null || reflectionId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void publishImageUpdateEvents(
            Long reflectionId,
            List<Long> removeImageIds,
            List<Image> addImages,
            DayReflection reflection) {
        if (!removeImageIds.isEmpty()) {
            eventPublisher.publishEvent(
                    new ReflectionImagesRemovedEvent(reflectionId, removeImageIds));
        }
        if (!addImages.isEmpty()) {
            eventPublisher.publishEvent(new ReflectionImagesAddedEvent(reflection, addImages));
        }
    }
}
