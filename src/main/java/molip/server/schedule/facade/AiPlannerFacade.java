package molip.server.schedule.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiPlannerClient;
import molip.server.ai.dto.request.AiPlannerRequest;
import molip.server.ai.dto.request.AiPlannerTaskRequest;
import molip.server.ai.dto.request.AiPlannerUserRequest;
import molip.server.ai.dto.response.AiPlannerResponse;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.dto.response.ScheduleArrangeResponse;
import molip.server.schedule.dto.response.ScheduleArrangeResultResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.service.DayPlanService;
import molip.server.schedule.service.ScheduleService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AiPlannerFacade {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DayPlanService dayPlanService;
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final AiPlannerClient aiPlannerClient;

    @Transactional
    public ScheduleArrangeResponse arrangeSchedules(Long userId, Long dayPlanId) {

        DayPlan dayPlan = dayPlanService.getDayPlan(userId, dayPlanId);
        Users user = userService.getUser(userId);

        LocalDateTime startArrangeDateTime = resolveStartArrangeDateTime(dayPlan);

        List<Schedule> schedules =
                scheduleService.getAiInputSchedules(userId, dayPlan, startArrangeDateTime);

        if (schedules.isEmpty()) {
            throw new BaseException(ErrorCode.PLANNER_BAD_REQUEST);
        }

        AiPlannerRequest request =
                new AiPlannerRequest(
                        new AiPlannerUserRequest(
                                user.getId(),
                                user.getFocusTimeZone().name(),
                                formatTime(user.getDayEndTime())),
                        formatTime(startArrangeDateTime.toLocalTime()),
                        schedules.stream().map(this::toAiTaskRequest).toList());

        AiPlannerResponse response = aiPlannerClient.requestPlanner(request);

        if (response == null || !response.success() || response.results() == null) {
            throw new BaseException(ErrorCode.PLANNER_INTERNAL_SERVER_ERROR);
        }

        List<Schedule> updatedSchedules =
                scheduleService.applyAiArrangement(userId, dayPlan, response.results());

        List<Schedule> responseSchedules = mergeSchedulesForResponse(schedules, updatedSchedules);

        return new ScheduleArrangeResponse(
                responseSchedules.stream()
                        .filter(schedule -> schedule.getStatus() != ScheduleStatus.SPLIT_PARENT)
                        .map(this::toResultResponse)
                        .toList());
    }

    private LocalDateTime resolveStartArrangeDateTime(DayPlan dayPlan) {

        LocalDate today = LocalDate.now();
        if (!dayPlan.getPlanDate().equals(today)) {
            return LocalDateTime.of(dayPlan.getPlanDate(), LocalTime.of(9, 0));
        }

        return LocalDateTime.now().plusMinutes(10);
    }

    private AiPlannerTaskRequest toAiTaskRequest(Schedule schedule) {

        Long parentScheduleId =
                schedule.getParentSchedule() == null ? null : schedule.getParentSchedule().getId();

        return new AiPlannerTaskRequest(
                schedule.getId(),
                parentScheduleId,
                schedule.getDayPlan().getId(),
                schedule.getTitle(),
                schedule.getType().name(),
                formatTime(schedule.getStartAt()),
                formatTime(schedule.getEndAt()),
                schedule.getEstimatedTimeRange() == null
                        ? null
                        : schedule.getEstimatedTimeRange().name(),
                schedule.getFocusLevel(),
                schedule.getIsUrgent());
    }

    private ScheduleArrangeResultResponse toResultResponse(Schedule schedule) {

        return new ScheduleArrangeResultResponse(
                schedule.getId(),
                schedule.getDayPlan().getId(),
                schedule.getTitle(),
                schedule.getType(),
                schedule.getAssignedBy(),
                schedule.getAssignmentStatus(),
                formatTime(schedule.getStartAt()),
                formatTime(schedule.getEndAt()));
    }

    private String formatTime(LocalTime time) {

        if (time == null) {
            return null;
        }

        return time.format(TIME_FORMATTER);
    }

    private List<Schedule> mergeSchedulesForResponse(
            List<Schedule> inputSchedules, List<Schedule> updatedSchedules) {

        Map<Long, Schedule> scheduleMap = new java.util.LinkedHashMap<>();

        for (Schedule schedule : updatedSchedules) {
            scheduleMap.put(schedule.getId(), schedule);
        }

        for (Schedule schedule : inputSchedules) {
            scheduleMap.putIfAbsent(schedule.getId(), schedule);
        }

        return scheduleMap.values().stream().toList();
    }
}
