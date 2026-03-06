package molip.server.schedule.facade;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.enums.ScheduleType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.dto.request.ScheduleChildrenCreateItem;
import molip.server.schedule.dto.response.ScheduleChildrenCreateGroupResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.enums.ScheduleActionType;
import molip.server.schedule.service.DayPlanService;
import molip.server.schedule.service.ScheduleActionLogService;
import molip.server.schedule.service.ScheduleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduleCommandFacade {

    private final ScheduleService scheduleService;
    private final DayPlanService dayPlanService;
    private final ScheduleActionLogService scheduleActionLogService;

    @Transactional
    public Schedule createSchedule(
            Long userId,
            Long dayPlanId,
            ScheduleType type,
            String title,
            LocalTime startAt,
            LocalTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {

        DayPlan dayPlan = dayPlanService.getDayPlan(userId, dayPlanId);

        Schedule created =
                scheduleService.createSchedule(
                        dayPlan,
                        type,
                        title,
                        startAt,
                        endAt,
                        estimatedTimeRange,
                        focusLevel,
                        isUrgent);

        scheduleActionLogService.publish(
                userId,
                created.getId(),
                ScheduleActionType.CREATE,
                "/day-plan/{dayPlanId}/schedule");

        return created;
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

        scheduleService.updateSchedule(
                userId,
                scheduleId,
                type,
                title,
                startAt,
                endAt,
                estimatedTimeRange,
                focusLevel,
                isUrgent);

        scheduleActionLogService.publish(
                userId, scheduleId, ScheduleActionType.UPDATE, "/schedule/{scheduleId}");
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        scheduleService.deleteSchedule(userId, scheduleId);

        scheduleActionLogService.publish(
                userId, scheduleId, ScheduleActionType.DELETE, "/schedule/{scheduleId}");
    }

    @Transactional
    public void updateStatus(Long userId, Long scheduleId, ScheduleStatus status) {
        scheduleService.updateStatus(userId, scheduleId, status);

        scheduleActionLogService.publish(
                userId, scheduleId, ScheduleActionType.UPDATE, "/schedule/{scheduleId}/status");
    }

    @Transactional
    public void assignScheduleToDayPlan(
            Long userId,
            Long scheduleId,
            Long targetDayPlanId,
            LocalTime startAt,
            LocalTime endAt) {

        scheduleService.assignScheduleToDayPlan(
                userId, scheduleId, targetDayPlanId, startAt, endAt);

        scheduleActionLogService.publish(
                userId, scheduleId, ScheduleActionType.UPDATE, "/schedule/{scheduleId}");
    }

    @Transactional
    public void updateAssignmentStatus(
            Long userId, Long targetScheduleId, Long excludedScheduleId) {
        scheduleService.updateAssignmentStatus(userId, targetScheduleId, excludedScheduleId);

        scheduleActionLogService.publish(
                userId,
                targetScheduleId,
                ScheduleActionType.UPDATE,
                "/schedule/{targetScheduleId}/assignment-status");
    }

    @Transactional
    public List<ScheduleChildrenCreateGroupResponse> createChildrenBatch(
            Long userId, List<ScheduleChildrenCreateItem> schedules) {

        validateChildrenBatchRequest(schedules);

        List<Schedule> results = new ArrayList<>();
        HashSet<Long> parentScheduleIds = new HashSet<>();

        for (ScheduleChildrenCreateItem scheduleRequest : schedules) {

            validateChildrenBatchItem(scheduleRequest);
            validateDuplicateParentScheduleId(
                    parentScheduleIds, scheduleRequest.parentScheduleId());

            results.addAll(
                    scheduleService.createChildren(
                            userId, scheduleRequest.parentScheduleId(), scheduleRequest.titles()));
        }

        return ScheduleChildrenCreateGroupResponse.groupFrom(results);
    }

    private void validateChildrenBatchRequest(List<ScheduleChildrenCreateItem> schedules) {

        if (schedules == null || schedules.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private void validateChildrenBatchItem(ScheduleChildrenCreateItem scheduleRequest) {

        if (scheduleRequest == null
                || scheduleRequest.parentScheduleId() == null
                || scheduleRequest.titles() == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private void validateDuplicateParentScheduleId(
            HashSet<Long> parentScheduleIds, Long parentScheduleId) {

        if (!parentScheduleIds.add(parentScheduleId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DUPLICATED_PARENT_SCHEDULE);
        }
    }
}
