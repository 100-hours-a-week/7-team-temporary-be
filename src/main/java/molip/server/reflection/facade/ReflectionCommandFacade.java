package molip.server.reflection.facade;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.entity.Image;
import molip.server.image.repository.ImageRepository;
import molip.server.reflection.dto.request.ReflectionCreateRequest;
import molip.server.reflection.dto.response.ReflectionCreateResponse;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.repository.DayReflectionRepository;
import molip.server.reflection.service.ReflectionService;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.repository.DayPlanRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReflectionCommandFacade {

    private final DayPlanRepository dayPlanRepository;
    private final DayReflectionRepository dayReflectionRepository;
    private final ImageRepository imageRepository;
    private final ReflectionService reflectionService;

    @Transactional
    public ReflectionCreateResponse createReflection(
            Long userId, Long dayPlanId, ReflectionCreateRequest request) {

        List<Long> imageIds = request.reflectionImageIds();

        DayPlan dayPlan =
                dayPlanRepository
                        .findByIdAndDeletedAtIsNull(dayPlanId)
                        .orElseThrow(
                                () -> new BaseException(ErrorCode.DAYPLAN_NOT_FOUND_REFLECTION));

        validateOwnership(dayPlan, userId);
        validateNotExists(dayPlanId);

        List<Image> images = resolveImages(imageIds);

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

    private List<Image> resolveImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueImageIds = uniqueIds(imageIds);
        List<Image> images = imageRepository.findByIdInAndDeletedAtIsNull(uniqueImageIds);

        if (images.size() != uniqueImageIds.size()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REFLECTION_IMAGES);
        }

        return images;
    }

    private List<Long> uniqueIds(List<Long> imageIds) {
        Set<Long> unique = new LinkedHashSet<>(imageIds);

        return new ArrayList<>(unique);
    }
}
