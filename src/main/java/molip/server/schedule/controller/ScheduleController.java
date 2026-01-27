package molip.server.schedule.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.schedule.dto.request.ScheduleArrangementJobCreateRequest;
import molip.server.schedule.dto.request.ScheduleAssignmentStatusUpdateRequest;
import molip.server.schedule.dto.request.ScheduleChildrenCreateRequest;
import molip.server.schedule.dto.request.ScheduleCreateRequest;
import molip.server.schedule.dto.request.ScheduleStatusUpdateRequest;
import molip.server.schedule.dto.request.ScheduleUpdateRequest;
import molip.server.schedule.dto.response.DayPlanSchedulePageResponse;
import molip.server.schedule.dto.response.ScheduleArrangementJobResponse;
import molip.server.schedule.dto.response.ScheduleChildrenCreateResponse;
import molip.server.schedule.dto.response.ScheduleCreateResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.facade.DayPlanQueryFacade;
import molip.server.schedule.facade.ScheduleQueryFacade;
import molip.server.schedule.service.DayPlanService;
import molip.server.schedule.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScheduleController implements ScheduleApi {

    private final ScheduleService scheduleService;
    private final DayPlanService dayPlanService;
    private final DayPlanQueryFacade dayPlanQueryFacade;
    private final ScheduleQueryFacade scheduleQueryFacade;

    @PostMapping("/day-plan/{dayPlanId}/schedule")
    @Override
    public ResponseEntity<ServerResponse<ScheduleCreateResponse>> createSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long dayPlanId,
            @RequestBody ScheduleCreateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        DayPlan dayPlan = dayPlanService.getDayPlan(userId, dayPlanId);

        Schedule schedule =
                scheduleService.createSchedule(
                        dayPlan,
                        request.type(),
                        request.title(),
                        request.startAt(),
                        request.endAt(),
                        request.estimatedTimeRange(),
                        request.focusLevel(),
                        request.isUrgent());

        return ResponseEntity.ok(
                ServerResponse.success(
                        SuccessCode.SCHEDULE_CREATED, ScheduleCreateResponse.from(schedule)));
    }

    @GetMapping("/day-plan/{dayPlanId}/schedule")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ScheduleSummaryResponse>>>
            getAllSchedulesByDayPlan(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @PathVariable Long dayPlanId,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/day-plan/schedule")
    @Override
    public ResponseEntity<ServerResponse<DayPlanSchedulePageResponse>>
            getOnlyTimeAssignedSchedulesByDate(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @RequestParam String date,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());
        DayPlan dayPlan = dayPlanQueryFacade.getOrCreateDayPlan(userId, date);
        DayPlanSchedulePageResponse response =
                scheduleQueryFacade.getTimeAssignedSchedulesByDate(dayPlan.getId(), page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.DAY_SCHEDULE_LIST_SUCCESS, response));
    }

    @PutMapping("/schedule/{scheduleId}")
    @Override
    public ResponseEntity<Void> updateSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        scheduleService.updateSchedule(
                userId,
                scheduleId,
                request.type(),
                request.title(),
                request.startAt(),
                request.endAt(),
                request.estimatedTimeRange(),
                request.focusLevel(),
                request.isUrgent());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @Override
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/day-plan/{dayPlanId}/schedule/arrangement-jobs")
    @Override
    public ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> createArrangementJob(
            @PathVariable Long dayPlanId,
            @RequestBody ScheduleArrangementJobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @GetMapping("/schedule/arrangement-jobs/{jobId}")
    @Override
    public ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> getArrangementJob(
            @PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PostMapping("/schedule/{scheduleId}/children")
    @Override
    public ResponseEntity<ServerResponse<ScheduleChildrenCreateResponse>> createChildren(
            @PathVariable Long scheduleId, @RequestBody ScheduleChildrenCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PatchMapping("/schedule/{scheduleId}/status")
    @Override
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long scheduleId, @RequestBody ScheduleStatusUpdateRequest request) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedules")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ScheduleSummaryResponse>>>
            getExcludedSchedules(
                    @RequestParam String status,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PatchMapping("/schedule/{scheduleId}/assignment-status")
    @Override
    public ResponseEntity<Void> updateAssignmentStatus(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleAssignmentStatusUpdateRequest request) {
        return ResponseEntity.noContent().build();
    }
}
