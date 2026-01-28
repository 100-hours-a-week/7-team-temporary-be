package molip.server.schedule.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageImpl;
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
            LocalTime startAt,
            LocalTime endAt,
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
            LocalTime startAt,
            LocalTime endAt,
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

    @Transactional(readOnly = true)
    public Page<Schedule> getExcludedSchedules(
            Long userId, String status, LocalDate fromDate, LocalDate toDate, int page, int size) {

        validatePage(page, size);

        AssignmentStatus assignmentStatus = parseAssignmentStatus(status);
        validateExcludedStatus(assignmentStatus);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        return scheduleRepository.findExcludedSchedulesByUserAndDateRange(
                userId, assignmentStatus, fromDate, toDate, pageRequest);
    }

    @Transactional(readOnly = true)
    public Page<Schedule> getTodoListSchedules(
            Long userId, LocalDate fromDate, LocalDate toDate, int page, int size) {

        validatePage(page, size);

        List<Schedule> todaySchedules =
                scheduleRepository.findTodoListSchedulesByUserAndPlanDateExcludingStatus(
                        userId, toDate, AssignmentStatus.EXCLUDED, ScheduleStatus.SPLIT_PARENT);

        List<Schedule> excludedSchedules =
                scheduleRepository.findTodoListExcludedSchedulesByUserAndDateRange(
                        userId,
                        fromDate,
                        toDate,
                        AssignmentStatus.EXCLUDED,
                        ScheduleStatus.SPLIT_PARENT);

        List<Schedule> mergedSchedules =
                new ArrayList<>(todaySchedules.size() + excludedSchedules.size());

        mergedSchedules.addAll(todaySchedules);
        mergedSchedules.addAll(excludedSchedules);

        int totalElements = mergedSchedules.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Schedule> pageContent =
                fromIndex >= totalElements
                        ? List.of()
                        : mergedSchedules.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, PageRequest.of(page - 1, size), totalElements);
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule schedule =
                scheduleRepository
                        .findByIdWithDayPlanUserIncludeDeleted(scheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        validateOwnership(userId, schedule);

        if (schedule.getDeletedAt() != null) {
            throw new BaseException(ErrorCode.CONFLICT_SCHEDULE_ALREADY_DELETED);
        }

        schedule.deleteSchedule();
    }

    @Transactional
    public void updateAssignmentStatus(
            Long userId, Long targetScheduleId, Long excludedScheduleId) {

        if (targetScheduleId.equals(excludedScheduleId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }

        Schedule targetSchedule =
                scheduleRepository
                        .findByIdWithDayPlanUser(targetScheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        Schedule excludedSchedule =
                scheduleRepository
                        .findByIdWithDayPlanUser(excludedScheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        validateOwnership(userId, targetSchedule);
        validateOwnership(userId, excludedSchedule);

        validateAssignmentSwap(targetSchedule, excludedSchedule);

        excludedSchedule.moveDayPlan(targetSchedule.getDayPlan());
        excludedSchedule.updateTime(targetSchedule.getStartAt(), targetSchedule.getEndAt());
        excludedSchedule.updateAssignmentStatus(AssignmentStatus.ASSIGNED);

        targetSchedule.updateAssignmentStatus(AssignmentStatus.EXCLUDED);
    }

    private void validateAssignmentSwap(Schedule targetSchedule, Schedule excludedSchedule) {
        if (excludedSchedule.getAssignmentStatus() != AssignmentStatus.EXCLUDED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }

        if (targetSchedule.getAssignmentStatus() == AssignmentStatus.EXCLUDED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }

        if (targetSchedule.getStartAt() == null || targetSchedule.getEndAt() == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private ScheduleCreator resolveCreator(ScheduleType type) {
        if (type == null || !creatorMap.containsKey(type)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        return Objects.requireNonNull(creatorMap.get(type));
    }

    private AssignmentStatus parseAssignmentStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }
        try {
            return AssignmentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
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

    private void validateFixedRange(LocalTime startAt, LocalTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        if (!endAt.isAfter(startAt)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_TIME_RANGE);
        }
    }

    private void validateTimeOverlap(
            Schedule schedule, Long scheduleId, LocalTime startAt, LocalTime endAt) {
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

    private void validateExcludedStatus(AssignmentStatus status) {
        if (status == null || status != AssignmentStatus.EXCLUDED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }
    }
}
