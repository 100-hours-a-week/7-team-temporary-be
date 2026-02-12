package molip.server.reflection.facade;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ImageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ImageInfoResponse;
import molip.server.common.response.PageResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.service.ImageService;
import molip.server.reflection.dto.response.ReflectionDetailResponse;
import molip.server.reflection.dto.response.ReflectionExistResponse;
import molip.server.reflection.dto.response.ReflectionListItemResponse;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.service.ReflectionImageService;
import molip.server.reflection.service.ReflectionService;
import molip.server.schedule.service.DayPlanService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReflectionQueryFacade {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ImageService imageService;
    private final DayPlanService dayPlanService;
    private final ReflectionService reflectionService;
    private final ReflectionImageService reflectionImageService;

    @Transactional(readOnly = true)
    public ReflectionDetailResponse getOpenReflectionDetail(Long reflectionId) {

        DayReflection reflection = reflectionService.getOpenReflection(reflectionId);

        List<ImageInfoResponse> images = resolveImages(reflectionId);
        long likes = reflectionService.countLikes(reflectionId);

        return ReflectionDetailResponse.of(
                reflection.getUser().getId(),
                reflection.getId(),
                reflection.isOpen(),
                reflection.getTitle(),
                reflection.getContent(),
                Math.toIntExact(likes),
                images,
                reflection.getCreatedAt().atZone(ZONE_ID).toOffsetDateTime());
    }

    @Transactional(readOnly = true)
    public ReflectionExistResponse existsReflection(Long userId, Long dayPlanId) {

        dayPlanService.getDayPlan(userId, dayPlanId);

        boolean alreadyWrote = reflectionService.existsByDayPlanId(dayPlanId);

        return ReflectionExistResponse.from(alreadyWrote);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReflectionListItemResponse> getMyReflections(
            Long userId, int page, int size) {

        validatePage(page, size);

        Page<DayReflection> reflections = reflectionService.getMyReflections(userId, page, size);

        return buildReflectionListResponse(reflections, page, size, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReflectionListItemResponse> getOpenReflections(
            Long viewerId, boolean isOpen, int page, int size) {

        validateOpenOnly(isOpen);
        validatePage(page, size);

        Page<DayReflection> reflections = reflectionService.getOpenReflections(page, size);

        return buildReflectionListResponse(reflections, page, size, viewerId);
    }

    private List<ImageInfoResponse> resolveImages(Long reflectionId) {
        List<DayReflectionImage> reflectionImages =
                reflectionImageService.getImagesByReflectionId(reflectionId);

        return reflectionImages.stream()
                .map(
                        item -> {
                            String uploadKey = item.getImage().getUploadKey();
                            ImageGetUrlResponse presigned =
                                    imageService.issueGetUrl(ImageType.REFLECTIONS, uploadKey);
                            return ImageInfoResponse.of(
                                    presigned.url(), presigned.expiresAt(), presigned.imageKey());
                        })
                .toList();
    }

    private Map<Long, List<ImageInfoResponse>> resolveImagesByReflectionIds(
            List<Long> reflectionIds) {
        Map<Long, List<DayReflectionImage>> reflectionImages =
                reflectionImageService.getImagesByReflectionIds(reflectionIds);

        return reflectionImages.entrySet().stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        entry.getValue().stream()
                                                .map(
                                                        item -> {
                                                            String uploadKey =
                                                                    item.getImage().getUploadKey();
                                                            ImageGetUrlResponse presigned =
                                                                    imageService.issueGetUrl(
                                                                            ImageType.REFLECTIONS,
                                                                            uploadKey);
                                                            return ImageInfoResponse.of(
                                                                    presigned.url(),
                                                                    presigned.expiresAt(),
                                                                    presigned.imageKey());
                                                        })
                                                .toList()));
    }

    private PageResponse<ReflectionListItemResponse> buildReflectionListResponse(
            Page<DayReflection> reflections, int page, int size, Long viewerId) {

        List<Long> reflectionIds =
                reflections.getContent().stream().map(DayReflection::getId).toList();

        Map<Long, List<ImageInfoResponse>> imagesByReflectionId =
                resolveImagesByReflectionIds(reflectionIds);

        List<ReflectionListItemResponse> content =
                reflections.getContent().stream()
                        .map(
                                reflection -> {
                                    List<ImageInfoResponse> images =
                                            imagesByReflectionId.getOrDefault(
                                                    reflection.getId(), List.of());
                                    long likes = reflectionService.countLikes(reflection.getId());
                                    boolean isMine =
                                            viewerId != null
                                                    && viewerId.equals(
                                                            reflection.getUser().getId());
                                    String ownerNickname = reflection.getUser().getNickname();

                                    return ReflectionListItemResponse.of(
                                            isMine,
                                            ownerNickname,
                                            reflection.getId(),
                                            reflection.isOpen(),
                                            reflection.getTitle(),
                                            reflection.getContent(),
                                            Math.toIntExact(likes),
                                            images,
                                            reflection
                                                    .getCreatedAt()
                                                    .atZone(ZONE_ID)
                                                    .toOffsetDateTime());
                                })
                        .toList();

        return new PageResponse<>(
                content, page, size, reflections.getTotalElements(), reflections.getTotalPages());
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }

    private void validateOpenOnly(boolean isOpen) {
        if (!isOpen) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_OPEN_ONLY);
        }
    }
}
