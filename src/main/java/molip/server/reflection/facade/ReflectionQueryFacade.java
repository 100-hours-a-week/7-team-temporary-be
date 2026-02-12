package molip.server.reflection.facade;

import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ImageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ImageInfoResponse;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.service.ImageService;
import molip.server.reflection.dto.response.ReflectionDetailResponse;
import molip.server.reflection.dto.response.ReflectionExistResponse;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.repository.DayReflectionImageRepository;
import molip.server.reflection.repository.DayReflectionRepository;
import molip.server.schedule.service.DayPlanService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReflectionQueryFacade {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final DayReflectionRepository dayReflectionRepository;
    private final DayReflectionImageRepository dayReflectionImageRepository;
    private final ImageService imageService;
    private final DayPlanService dayPlanService;

    @Transactional(readOnly = true)
    public ReflectionDetailResponse getOpenReflectionDetail(Long reflectionId) {

        DayReflection reflection =
                dayReflectionRepository
                        .findByIdAndDeletedAtIsNull(reflectionId)
                        .orElseThrow(() -> new BaseException(ErrorCode.REFLECTION_NOT_FOUND));

        if (!reflection.isOpen()) {
            throw new BaseException(ErrorCode.FORBIDDEN_REFLECTION_OPEN_ONLY);
        }

        List<ImageInfoResponse> images = resolveImages(reflectionId);
        long likes = dayReflectionRepository.countLikes(reflectionId);

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

        boolean alreadyWrote =
                dayReflectionRepository.existsByDayPlanIdAndDeletedAtIsNull(dayPlanId);

        return ReflectionExistResponse.from(alreadyWrote);
    }

    private List<ImageInfoResponse> resolveImages(Long reflectionId) {
        List<DayReflectionImage> reflectionImages =
                dayReflectionImageRepository.findByReflectionIdWithImage(reflectionId);

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
}
