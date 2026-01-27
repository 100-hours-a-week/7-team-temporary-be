package molip.server.schedule.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.response.PageResponse;
import molip.server.schedule.dto.response.DayPlanSchedulePageResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.service.ScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduleQueryFacade {
    private final ScheduleService scheduleService;

    @Transactional(readOnly = true)
    public PageResponse<ScheduleSummaryResponse> getTimeAssignedSchedulesByDayPlan(
            Long dayPlanId, int page, int size) {
        Page<Schedule> schedules =
                scheduleService.getTimeAssignedSchedulesByDayPlan(dayPlanId, page, size);
        Page<ScheduleSummaryResponse> mapped =
                schedules.map(
                        schedule ->
                                ScheduleSummaryResponse.from(
                                        schedule, schedule.getParentSchedule()));
        return PageResponse.from(mapped, page, size);
    }

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
}
