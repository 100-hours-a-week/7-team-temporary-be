package molip.server.schedule.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleStatus;
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

        validateRequired(type, title);

        Schedule schedule =
                scheduleRepository
                        .findByIdWithDayPlanUser(scheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        validateOwnership(userId, schedule);

        if (type == ScheduleType.FIXED) {
            validateFixedRange(startAt, endAt);
            validateTimeOverlap(schedule, scheduleId, startAt, endAt);
            schedule.updateAsFixed(title, startAt, endAt);
        } else {
            schedule.updateAsFlex(title, estimatedTimeRange, focusLevel, isUrgent);
        }
    }

    @Transactional
    public void updateStatus(Long userId, Long scheduleId, ScheduleStatus status) {
        if (status == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        Schedule schedule =
                scheduleRepository
                        .findByIdWithDayPlanUser(scheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        validateOwnership(userId, schedule);

        schedule.updateStatus(status);
    }

    @Transactional
    public List<Schedule> createChildren(Long userId, Long parentScheduleId, List<String> titles) {
        validateChildrenTitles(titles);

        Schedule parentSchedule =
                scheduleRepository
                        .findByIdWithDayPlanUser(parentScheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND_PARENT));

        validateOwnership(userId, parentSchedule);

        validateSplitAllowed(parentSchedule);

        if (scheduleRepository.existsByParentScheduleIdAndDeletedAtIsNull(parentScheduleId)) {
            throw new BaseException(ErrorCode.CONFLICT_CHILDREN_ALREADY_EXISTS);
        }

        parentSchedule.updateStatus(ScheduleStatus.SPLIT_PARENT);

        List<Schedule> children =
                titles.stream()
                        .map(
                                title ->
                                        Schedule.builder()
                                                .dayPlan(parentSchedule.getDayPlan())
                                                .parentSchedule(parentSchedule)
                                                .title(title)
                                                .status(ScheduleStatus.TODO)
                                                .type(ScheduleType.FLEX)
                                                .assignedBy(AssignedBy.USER)
                                                .assignmentStatus(AssignmentStatus.NOT_ASSIGNED)
                                                .estimatedTimeRange(EstimatedTimeRange.HOUR_1_TO_2)
                                                .focusLevel(parentSchedule.getFocusLevel())
                                                .isUrgent(parentSchedule.getIsUrgent())
                                                .build())
                        .toList();

        return scheduleRepository.saveAll(children);
    }

    private void validateRequired(ScheduleType type, String title) {
        if (type == null || title == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private void validateOwnership(Long userId, Schedule schedule) {
        if (!schedule.getDayPlan().getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_OWN_SCHEDULE_ONLY);
        }
    }

    private void validateFixedRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        if (!endAt.isAfter(startAt)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_TIME_RANGE);
        }
    }

    private void validateTimeOverlap(
            Schedule schedule, Long scheduleId, LocalDateTime startAt, LocalDateTime endAt) {
        if (scheduleRepository.existsTimeOverlapExcludingId(
                schedule.getDayPlan().getId(), scheduleId, startAt, endAt)) {
            throw new BaseException(ErrorCode.CONFLICT_TIME_OVERLAP);
        }
    }

    private void validateChildrenTitles(List<String> titles) {
        if (titles == null || titles.size() < 2) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_CHILDREN_MIN);
        }
    }

    private void validateSplitAllowed(Schedule parentSchedule) {
        if (parentSchedule.getType() == ScheduleType.FIXED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_FIXED_SCHEDULE_SPLIT);
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
