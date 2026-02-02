package molip.server.schedule.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import molip.server.ai.dto.response.AiPlannerChildResponse;
import molip.server.ai.dto.response.AiPlannerResultResponse;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleHistoryEventType;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.enums.ScheduleType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.notification.event.NotificationCreatedEvent;
import molip.server.notification.event.ScheduleReminderResetEvent;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.entity.ScheduleHistory;
import molip.server.schedule.event.ScheduleHistoryRecordedEvent;
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.schedule.service.strategy.ScheduleCreator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final DayPlanService dayPlanService;
    private final Map<ScheduleType, ScheduleCreator> creatorMap;
    private final ApplicationEventPublisher eventPublisher;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            DayPlanService dayPlanService,
            List<ScheduleCreator> creators,
            ApplicationEventPublisher eventPublisher) {

        this.scheduleRepository = scheduleRepository;
        this.dayPlanService = dayPlanService;
        this.creatorMap = new EnumMap<>(ScheduleType.class);
        this.eventPublisher = eventPublisher;

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

        Schedule savedSchedule = scheduleRepository.save(schedule);

        publishScheduleCreatedEvent(savedSchedule);

        return savedSchedule;
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
            schedule.updateAsFlex(title, startAt, endAt, estimatedTimeRange, focusLevel, isUrgent);
        }

        eventPublisher.publishEvent(
                new ScheduleReminderResetEvent(
                        schedule.getId(),
                        schedule.getDayPlan().getUser().getId(),
                        schedule.getTitle(),
                        schedule.getDayPlan().getPlanDate(),
                        schedule.getStartAt()));
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
        validateChildrenNotExists(parentScheduleId);

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
                scheduleRepository.findSchedulesByUserAndPlanDateExcludingStatuses(
                        userId,
                        toDate,
                        List.of(ScheduleStatus.SPLIT_PARENT, ScheduleStatus.DONE),
                        List.of(AssignmentStatus.EXCLUDED));

        List<Schedule> excludedSchedules =
                scheduleRepository.findSchedulesByUserAndDateRangeAndStatusInExcludingStatuses(
                        userId,
                        List.of(AssignmentStatus.EXCLUDED),
                        fromDate,
                        toDate,
                        List.of(ScheduleStatus.SPLIT_PARENT, ScheduleStatus.DONE));

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

    @Transactional(readOnly = true)
    public List<Schedule> getAiInputSchedules(
            Long userId, DayPlan dayPlan, LocalDateTime startArrangeDateTime) {

        LocalDate planDate = dayPlan.getPlanDate();
        LocalDate yesterday = planDate.minusDays(1);

        List<Schedule> todaySchedules =
                scheduleRepository.findSchedulesByDayPlanIdAndStatusInExcludingStatusesAfterTime(
                        dayPlan.getId(),
                        List.of(
                                AssignmentStatus.ASSIGNED,
                                AssignmentStatus.NOT_ASSIGNED,
                                AssignmentStatus.EXCLUDED),
                        List.of(ScheduleStatus.SPLIT_PARENT, ScheduleStatus.DONE),
                        startArrangeDateTime.toLocalTime());

        List<Schedule> yesterdayExcludedSchedules =
                scheduleRepository.findSchedulesByUserAndDateRangeAndStatusInExcludingStatuses(
                        userId,
                        List.of(AssignmentStatus.EXCLUDED),
                        yesterday,
                        yesterday,
                        List.of(ScheduleStatus.SPLIT_PARENT, ScheduleStatus.DONE));

        return mergeSchedules(todaySchedules, yesterdayExcludedSchedules);
    }

    @Transactional(readOnly = true)
    public Optional<Schedule> getCurrentSchedule(Long dayPlanId, LocalTime currentTime) {

        validateCurrentScheduleParams(dayPlanId, currentTime);

        List<ScheduleStatus> excludeStatuses = List.of(ScheduleStatus.SPLIT_PARENT);

        List<AssignmentStatus> excludeAssignmentStatuses = List.of(AssignmentStatus.EXCLUDED);

        return scheduleRepository.findCurrentSchedule(
                dayPlanId, currentTime, excludeStatuses, excludeAssignmentStatuses);
    }

    @Transactional
    public List<Schedule> applyAiArrangement(
            Long userId, DayPlan targetDayPlan, List<AiPlannerResultResponse> results) {

        List<Schedule> updatedSchedules = new ArrayList<>();

        for (AiPlannerResultResponse result : results) {

            Schedule schedule =
                    scheduleRepository
                            .findByIdWithDayPlanUser(result.taskId())
                            .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

            validateOwnership(userId, schedule);

            if (schedule.getType() == ScheduleType.FIXED) {
                continue;
            }

            LocalDate prevPlanDate = schedule.getDayPlan().getPlanDate();
            LocalTime prevStartAt = schedule.getStartAt();
            LocalTime prevEndAt = schedule.getEndAt();

            AssignmentStatus assignmentStatus = AssignmentStatus.valueOf(result.assignmentStatus());

            LocalTime startAt = parseTime(result.startAt());
            LocalTime endAt = parseTime(result.endAt());

            if (assignmentStatus == AssignmentStatus.EXCLUDED) {
                startAt = null;
                endAt = null;
            }

            if (assignmentStatus == AssignmentStatus.ASSIGNED
                    && !schedule.getDayPlan().getId().equals(targetDayPlan.getId())) {

                schedule.moveDayPlan(targetDayPlan);
            }

            schedule.applyAiResult(AssignedBy.AI, assignmentStatus, result.title(), startAt, endAt);

            List<AiPlannerChildResponse> children = result.children();

            if (children != null && !children.isEmpty()) {

                schedule.updateStatus(ScheduleStatus.SPLIT_PARENT);

                List<Schedule> childSchedules =
                        createChildrenFromAi(schedule, targetDayPlan, children);

                updatedSchedules.addAll(childSchedules);
            }

            recordScheduleHistory(
                    schedule,
                    prevPlanDate,
                    prevStartAt,
                    prevEndAt,
                    schedule.getDayPlan().getPlanDate(),
                    schedule.getStartAt(),
                    schedule.getEndAt());

            updatedSchedules.add(schedule);
        }

        return updatedSchedules;
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
        targetSchedule.updateType(ScheduleType.FLEX);
        targetSchedule.updateTime(null, null);
    }

    private List<Schedule> createChildrenFromAi(
            Schedule parentSchedule, DayPlan targetDayPlan, List<AiPlannerChildResponse> children) {

        if (scheduleRepository.existsByParentScheduleIdAndDeletedAtIsNull(parentSchedule.getId())) {
            throw new BaseException(ErrorCode.CONFLICT_CHILDREN_ALREADY_EXISTS);
        }

        List<Schedule> childSchedules =
                children.stream()
                        .map(
                                child ->
                                        Schedule.builder()
                                                .dayPlan(targetDayPlan)
                                                .parentSchedule(parentSchedule)
                                                .title(child.title())
                                                .status(ScheduleStatus.TODO)
                                                .type(ScheduleType.FLEX)
                                                .assignedBy(AssignedBy.AI)
                                                .assignmentStatus(AssignmentStatus.ASSIGNED)
                                                .startAt(parseTime(child.startAt()))
                                                .endAt(parseTime(child.endAt()))
                                                .estimatedTimeRange(null)
                                                .focusLevel(null)
                                                .isUrgent(null)
                                                .build())
                        .toList();

        List<Schedule> savedChildren = scheduleRepository.saveAll(childSchedules);

        for (Schedule child : savedChildren) {
            recordScheduleHistory(
                    child,
                    null,
                    null,
                    null,
                    child.getDayPlan().getPlanDate(),
                    child.getStartAt(),
                    child.getEndAt());
        }

        return savedChildren;
    }

    private LocalTime parseTime(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalTime.parse(value);
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

    @Transactional
    public void assignScheduleToDayPlan(
            Long userId,
            Long scheduleId,
            Long targetDayPlanId,
            LocalTime startAt,
            LocalTime endAt) {

        if (targetDayPlanId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        validateFixedRange(startAt, endAt);

        Schedule schedule =
                scheduleRepository
                        .findByIdWithDayPlanUser(scheduleId)
                        .orElseThrow(() -> new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        validateOwnership(userId, schedule);

        if (schedule.getType() != ScheduleType.FLEX) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_EXCLUDED_ONLY_FLEX);
        }

        DayPlan targetDayPlan = dayPlanService.getDayPlan(userId, targetDayPlanId);

        validateTimeOverlapForTarget(targetDayPlan.getId(), scheduleId, startAt, endAt);

        if (!schedule.getDayPlan().getId().equals(targetDayPlan.getId())) {
            schedule.moveDayPlan(targetDayPlan);
        }

        schedule.updateAsFlex(
                schedule.getTitle(),
                startAt,
                endAt,
                schedule.getEstimatedTimeRange(),
                schedule.getFocusLevel(),
                schedule.getIsUrgent());

        eventPublisher.publishEvent(
                new ScheduleReminderResetEvent(
                        schedule.getId(),
                        schedule.getDayPlan().getUser().getId(),
                        schedule.getTitle(),
                        schedule.getDayPlan().getPlanDate(),
                        schedule.getStartAt()));
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

    private void publishScheduleCreatedEvent(Schedule schedule) {

        eventPublisher.publishEvent(
                new NotificationCreatedEvent(
                        schedule.getId(),
                        schedule.getDayPlan().getUser().getId(),
                        schedule.getTitle(),
                        schedule.getDayPlan().getPlanDate(),
                        schedule.getStartAt()));
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

    private void validateTimeOverlapForTarget(
            Long dayPlanId, Long scheduleId, LocalTime startAt, LocalTime endAt) {

        if (scheduleRepository.existsTimeOverlapExcludingId(
                dayPlanId, scheduleId, startAt, endAt)) {
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

    private void validateChildrenNotExists(Long parentScheduleId) {

        if (scheduleRepository.existsByParentScheduleIdAndDeletedAtIsNull(parentScheduleId)) {
            throw new BaseException(ErrorCode.CONFLICT_CHILDREN_ALREADY_EXISTS);
        }
    }

    private void validateExcludedStatus(AssignmentStatus status) {
        if (status == null || status != AssignmentStatus.EXCLUDED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_CHECK);
        }
    }

    private void validateCurrentScheduleParams(Long dayPlanId, LocalTime currentTime) {

        if (dayPlanId == null || currentTime == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private void recordScheduleHistory(
            Schedule schedule,
            LocalDate prevPlanDate,
            LocalTime prevStartAt,
            LocalTime prevEndAt,
            LocalDate nextPlanDate,
            LocalTime nextStartAt,
            LocalTime nextEndAt) {

        ScheduleHistoryEventType eventType =
                resolveHistoryEventType(prevStartAt, prevEndAt, nextStartAt, nextEndAt);

        if (eventType == null) {
            return;
        }

        ScheduleHistory history =
                new ScheduleHistory(
                        schedule,
                        eventType,
                        toDateTime(prevPlanDate, prevStartAt),
                        toDateTime(prevPlanDate, prevEndAt),
                        toDateTime(nextPlanDate, nextStartAt),
                        toDateTime(nextPlanDate, nextEndAt));

        eventPublisher.publishEvent(new ScheduleHistoryRecordedEvent(history));
    }

    private ScheduleHistoryEventType resolveHistoryEventType(
            LocalTime prevStartAt,
            LocalTime prevEndAt,
            LocalTime nextStartAt,
            LocalTime nextEndAt) {

        if (prevStartAt == null && prevEndAt == null && nextStartAt == null && nextEndAt == null) {
            return null;
        }

        if (prevStartAt == null && prevEndAt == null && nextStartAt != null && nextEndAt != null) {
            return ScheduleHistoryEventType.ASSIGN_TIME;
        }

        if (prevStartAt != null && prevEndAt != null && nextStartAt == null && nextEndAt == null) {
            return ScheduleHistoryEventType.MOVE_TIME;
        }

        if (prevStartAt != null && prevEndAt != null && nextStartAt != null && nextEndAt != null) {

            long prevDuration = Duration.between(prevStartAt, prevEndAt).toMinutes();
            long nextDuration = Duration.between(nextStartAt, nextEndAt).toMinutes();

            if (prevDuration != nextDuration) {
                return ScheduleHistoryEventType.CHANGE_DURATION;
            }

            if (!prevStartAt.equals(nextStartAt) || !prevEndAt.equals(nextEndAt)) {
                return ScheduleHistoryEventType.MOVE_TIME;
            }
        }

        return null;
    }

    private LocalDateTime toDateTime(LocalDate planDate, LocalTime time) {

        if (planDate == null || time == null) {
            return null;
        }

        return LocalDateTime.of(planDate, time);
    }

    private List<Schedule> mergeSchedules(
            List<Schedule> todaySchedules, List<Schedule> excludedSchedules) {

        Map<Long, Schedule> scheduleMap = new LinkedHashMap<>();

        for (Schedule schedule : todaySchedules) {
            scheduleMap.put(schedule.getId(), schedule);
        }

        for (Schedule schedule : excludedSchedules) {
            scheduleMap.putIfAbsent(schedule.getId(), schedule);
        }

        return scheduleMap.values().stream().toList();
    }
}
