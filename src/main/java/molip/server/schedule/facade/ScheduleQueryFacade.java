package molip.server.schedule.facade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.response.PageResponse;
import molip.server.schedule.dto.response.DayPlanSchedulePageResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.service.DayPlanService;
import molip.server.schedule.service.ScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduleQueryFacade {
    private final ScheduleService scheduleService;
    private final DayPlanService dayPlanService;

    @Transactional(readOnly = true)
    public DayPlanSchedulePageResponse getTimeAssignedSchedulesByDate(
            Long dayPlanId, int page, int size) {

        Page<Schedule> schedules =
                scheduleService.getTimeAssignedSchedulesByDayPlan(dayPlanId, page, size);

        List<ScheduleSummaryResponse> content =
                schedules.getContent().stream()
                        .map(
                                schedule ->
                                        ScheduleSummaryResponse.from(
                                                schedule, schedule.getParentSchedule()))
                        .toList();

        return DayPlanSchedulePageResponse.of(
                dayPlanId,
                content,
                page,
                size,
                schedules.getTotalElements(),
                schedules.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleSummaryResponse> getTodoSchedulesByDayPlan(
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

        return PageResponse.from(mapped, page, size);
    }

    @Transactional(readOnly = true)
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

        return PageResponse.from(mapped, page, size);
    }

    @Transactional(readOnly = true)
    public ScheduleSummaryResponse getCurrentSchedule(Long dayPlanId) {

        LocalTime currentTime = LocalTime.now();

        return scheduleService
                .getCurrentSchedule(dayPlanId, currentTime)
                .map(value -> ScheduleSummaryResponse.from(value, value.getParentSchedule()))
                .orElse(null);
    }
}
