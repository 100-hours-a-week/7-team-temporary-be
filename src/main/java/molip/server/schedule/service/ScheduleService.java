package molip.server.schedule.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.schedule.service.strategy.ScheduleCreator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final Map<ScheduleType, ScheduleCreator> creatorMap;

    public ScheduleService(ScheduleRepository scheduleRepository, List<ScheduleCreator> creators) {
        this.scheduleRepository = scheduleRepository;
        this.creatorMap = new EnumMap<>(ScheduleType.class);
        for (ScheduleCreator creator : creators) {
            this.creatorMap.put(creator.supports(), creator);
        }
    }

    @Transactional
    public Schedule createSchedule(
            DayPlan dayPlan,
            ScheduleType type,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {

        ScheduleCreator creator = resolveCreator(type);

        Schedule schedule =
                creator.create(
                        dayPlan, title, startAt, endAt, estimatedTimeRange, focusLevel, isUrgent);

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Page<Schedule> getTimeAssignedSchedulesByDayPlan(Long dayPlanId, int page, int size) {

        validatePage(page, size);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("startAt").ascending());

        return scheduleRepository.findTimeAssignedByDayPlanId(dayPlanId, pageRequest);
    }

    @Transactional
    public void updateSchedule(
            Long userId,
            Long scheduleId,
            ScheduleType type,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {
        if (type == null || title == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        Schedule schedule =
                scheduleRepository
                        .findByIdAndDeletedAtIsNull(scheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getDayPlan().getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_OWN_SCHEDULE_ONLY);
        }

        if (type == ScheduleType.FIXED) {
            if (startAt == null || endAt == null) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
            }
            if (!endAt.isAfter(startAt)) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_TIME_RANGE);
            }
            if (scheduleRepository.existsTimeOverlapExcludingId(
                    schedule.getDayPlan().getId(), scheduleId, startAt, endAt)) {
                throw new BaseException(ErrorCode.CONFLICT_TIME_OVERLAP);
            }
            schedule.updateAsFixed(title, startAt, endAt);
        } else {
            schedule.updateAsFlex(title, estimatedTimeRange, focusLevel, isUrgent);
        }
    }

    private ScheduleCreator resolveCreator(ScheduleType type) {
        if (type == null || !creatorMap.containsKey(type)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        return Objects.requireNonNull(creatorMap.get(type));
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }
}
