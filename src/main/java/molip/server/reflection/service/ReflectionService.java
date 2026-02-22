package molip.server.reflection.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.entity.Image;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.event.DayReflectionImagesCreateEvent;
import molip.server.reflection.repository.DayReflectionRepository;
import molip.server.schedule.entity.DayPlan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReflectionService {

    private static final DateTimeFormatter TITLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd(E)", Locale.KOREAN);

    private final DayReflectionRepository dayReflectionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxEventService outboxEventService;

    @Transactional
    public DayReflection createReflection(
            DayPlan dayPlan, String content, boolean isOpen, List<Image> images) {
        String title = formatTitle(dayPlan.getPlanDate());

        DayReflection reflection =
                dayReflectionRepository.save(
                        new DayReflection(dayPlan.getUser(), dayPlan, title, content, isOpen));

        if (images != null && !images.isEmpty()) {
            eventPublisher.publishEvent(new DayReflectionImagesCreateEvent(reflection, images));
        }

        outboxEventService.recordCreated(
                AggregateType.REFLECTION,
                reflection.getId(),
                OutboxPayloadMapper.reflection(reflection));
        return reflection;
    }

    @Transactional(readOnly = true)
    public DayReflection getOpenReflection(Long reflectionId) {
        DayReflection reflection =
                dayReflectionRepository
                        .findByIdAndDeletedAtIsNull(reflectionId)
                        .orElseThrow(() -> new BaseException(ErrorCode.REFLECTION_NOT_FOUND));

        if (!reflection.isOpen()) {
            throw new BaseException(ErrorCode.FORBIDDEN_REFLECTION_OPEN_ONLY);
        }

        return reflection;
    }

    @Transactional(readOnly = true)
    public DayReflection getReflection(Long reflectionId) {
        return dayReflectionRepository
                .findByIdAndDeletedAtIsNull(reflectionId)
                .orElseThrow(() -> new BaseException(ErrorCode.REFLECTION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<DayReflection> getMyReflections(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("updatedAt").descending());

        return dayReflectionRepository.findByUserIdAndDeletedAtIsNull(userId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Page<DayReflection> getOpenReflections(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("updatedAt").descending());

        return dayReflectionRepository.findByIsOpenTrueAndDeletedAtIsNull(pageRequest);
    }

    @Transactional(readOnly = true)
    public boolean existsByDayPlanId(Long dayPlanId) {
        return dayReflectionRepository.existsByDayPlanIdAndDeletedAtIsNull(dayPlanId);
    }

    @Transactional(readOnly = true)
    public long countLikes(Long reflectionId) {
        return dayReflectionRepository.countLikes(reflectionId);
    }

    @Transactional
    public void updateOpen(Long userId, Long reflectionId, Boolean isOpen) {
        validateUpdateOpen(userId, reflectionId, isOpen);

        DayReflection reflection =
                dayReflectionRepository
                        .findByIdAndDeletedAtIsNull(reflectionId)
                        .orElseThrow(() -> new BaseException(ErrorCode.REFLECTION_NOT_FOUND));

        if (!reflection.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REFLECTION_UPDATE);
        }

        reflection.updateOpen(isOpen);
        outboxEventService.recordUpdated(
                AggregateType.REFLECTION,
                reflection.getId(),
                OutboxPayloadMapper.reflection(reflection));
    }

    private String formatTitle(LocalDate planDate) {
        return planDate.format(TITLE_FORMATTER);
    }

    private void validateUpdateOpen(Long userId, Long reflectionId, Boolean isOpen) {
        if (userId == null || reflectionId == null || isOpen == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }
}
