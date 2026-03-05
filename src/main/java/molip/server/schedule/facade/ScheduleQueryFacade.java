package molip.server.schedule.facade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.response.PageResponse;
import molip.server.schedule.dto.response.DayPlanSchedulePageResponse;
import molip.server.schedule.dto.response.DayPlanTodoListResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.enums.ScheduleActionType;
import molip.server.schedule.service.DayPlanService;
import molip.server.schedule.service.ScheduleActionLogService;
import molip.server.schedule.service.ScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduleQueryFacade {
    private final ScheduleService scheduleService;
    private final DayPlanService dayPlanService;
    private final ScheduleActionLogService scheduleActionLogService;

    @Transactional
    public DayPlanSchedulePageResponse getTimeAssignedSchedulesByDate(
            Long userId, Long dayPlanId, int page, int size) {

        Page<Schedule> schedules =
                scheduleService.getTimeAssignedSchedulesByDayPlan(dayPlanId, page, size);

        List<ScheduleSummaryResponse> content =
                schedules.getContent().stream()
                        .map(
                                schedule ->
                                        ScheduleSummaryResponse.from(
                                                schedule, schedule.getParentSchedule()))
                        .toList();

        DayPlanSchedulePageResponse response =
                DayPlanSchedulePageResponse.of(
                        dayPlanId,
                        content,
                        page,
                        size,
                        schedules.getTotalElements(),
                        schedules.getTotalPages());

        scheduleActionLogService.publish(
                userId, null, ScheduleActionType.READ, "/day-plan/schedule");

        return response;
    }

    @Transactional
    public DayPlanTodoListResponse getTodoSchedulesByDayPlan(
            Long userId, Long dayPlanId, int page, int size) {

        DayPlan dayPlan = dayPlanService.getDayPlan(userId, dayPlanId);

        LocalDate toDate = dayPlan.getPlanDate();
        LocalDate fromDate = toDate.minusDays(1);

        Page<Schedule> schedules =
                scheduleService.getTodoListSchedules(userId, fromDate, toDate, page, size);

        Page<ScheduleSummaryResponse> mapped =
                schedules.map(
                        schedule ->
                                ScheduleSummaryResponse.from(
                                        schedule, schedule.getParentSchedule()));

        DayPlanTodoListResponse response =
                DayPlanTodoListResponse.of(
                        dayPlan.getId(),
                        dayPlan.getAiUsageRemainingCount(),
                        mapped.getContent(),
                        page,
                        size,
                        mapped.getTotalElements(),
                        mapped.getTotalPages());

        scheduleActionLogService.publish(
                userId, null, ScheduleActionType.READ, "/day-plan/{dayPlanId}/schedule");

        return response;
    }

    @Transactional
    public PageResponse<ScheduleSummaryResponse> getExcludedSchedules(
            Long userId, Long dayPlanId, String status, int page, int size) {

        DayPlan dayPlan = dayPlanService.getDayPlan(userId, dayPlanId);

        LocalDate toDate = dayPlan.getPlanDate();
        LocalDate fromDate = toDate.minusDays(1);

        Page<Schedule> schedules =
                scheduleService.getExcludedSchedules(userId, status, fromDate, toDate, page, size);

        Page<ScheduleSummaryResponse> mapped =
                schedules.map(
                        schedule ->
                                ScheduleSummaryResponse.from(
                                        schedule, schedule.getParentSchedule()));

        PageResponse<ScheduleSummaryResponse> response = PageResponse.from(mapped, page, size);

        scheduleActionLogService.publish(
                userId, null, ScheduleActionType.READ, "/day-plan/{dayPlanId}/schedules");

        return response;
    }

    @Transactional
    public ScheduleSummaryResponse getCurrentSchedule(Long userId, Long dayPlanId) {

        LocalTime currentTime = LocalTime.now();

        ScheduleSummaryResponse response =
                scheduleService
                        .getCurrentSchedule(dayPlanId, currentTime)
                        .map(
                                value ->
                                        ScheduleSummaryResponse.from(
                                                value, value.getParentSchedule()))
                        .orElse(null);

        scheduleActionLogService.publish(
                userId,
                response == null ? null : response.scheduleId(),
                ScheduleActionType.READ,
                "/schedule");

        return response;
    }
}
