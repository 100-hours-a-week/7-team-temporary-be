package molip.server.schedule.controller;

import java.time.LocalDate;
import java.util.List;
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
import molip.server.schedule.dto.response.ScheduleArrangeResponse;
import molip.server.schedule.dto.response.ScheduleArrangementJobResponse;
import molip.server.schedule.dto.response.ScheduleChildrenCreateGroupResponse;
import molip.server.schedule.dto.response.ScheduleCreateResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.facade.AiPlannerFacade;
import molip.server.schedule.facade.DayPlanQueryFacade;
import molip.server.schedule.facade.ScheduleCommandFacade;
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
    private final ScheduleCommandFacade scheduleCommandFacade;
    private final AiPlannerFacade aiPlannerFacade;

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

        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<ScheduleSummaryResponse> response =
                scheduleQueryFacade.getTodoSchedulesByDayPlan(userId, dayPlanId, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.DAY_TODO_LIST_SUCCESS, response));
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
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long scheduleId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        scheduleService.deleteSchedule(userId, scheduleId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/day-plan/{dayPlanId}/schedule/arrangement-jobs")
    @Deprecated
    @Override
    public ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> createArrangementJob(
            @PathVariable Long dayPlanId,
            @RequestBody ScheduleArrangementJobCreateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @GetMapping("/schedule/arrangement-jobs/{jobId}")
    @Deprecated
    @Override
    public ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> getArrangementJob(
            @PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @PostMapping("/schedule/children")
    @Override
    public ResponseEntity<ServerResponse<List<ScheduleChildrenCreateGroupResponse>>> createChildren(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ScheduleChildrenCreateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        List<ScheduleChildrenCreateGroupResponse> schedules =
                scheduleCommandFacade.createChildrenBatch(userId, request.schedules());

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.SCHEDULE_CHILDREN_CREATED, schedules));
    }

    @PatchMapping("/schedule/{scheduleId}/status")
    @Override
    public ResponseEntity<Void> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long scheduleId,
            @RequestBody ScheduleStatusUpdateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        scheduleService.updateStatus(userId, scheduleId, request.status());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day-plan/{dayPlanId}/schedules")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<ScheduleSummaryResponse>>>
            getExcludedSchedules(
                    @AuthenticationPrincipal UserDetails userDetails,
                    @PathVariable Long dayPlanId,
                    @RequestParam String status,
                    @RequestParam(required = false, defaultValue = "1") int page,
                    @RequestParam(required = false, defaultValue = "10") int size) {

        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<ScheduleSummaryResponse> response =
                scheduleQueryFacade.getExcludedSchedules(userId, dayPlanId, status, page, size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.EXCLUDED_SCHEDULE_LIST_SUCCESS, response));
    }

    @GetMapping("/schedule")
    @Override
    public ResponseEntity<ServerResponse<ScheduleSummaryResponse>> getCurrentSchedule(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.valueOf(userDetails.getUsername());

        String today = LocalDate.now().toString();
        DayPlan dayPlan = dayPlanQueryFacade.getOrCreateDayPlan(userId, today);

        ScheduleSummaryResponse response = scheduleQueryFacade.getCurrentSchedule(dayPlan.getId());

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.CURRENT_SCHEDULE_FETCH_SUCCESS, response));
    }

    @PostMapping("/day-plan/{dayPlanId}/schedules/ai-arrangement")
    @Override
    public ResponseEntity<ServerResponse<ScheduleArrangeResponse>> arrangeSchedules(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long dayPlanId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        ScheduleArrangeResponse response = aiPlannerFacade.arrangeSchedules(userId, dayPlanId);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.AI_ARRANGEMENT_COMPLETED, response));
    }

    @PatchMapping("/schedule/{targetScheduleId}/assignment-status")
    @Override
    public ResponseEntity<Void> updateAssignmentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetScheduleId,
            @RequestBody ScheduleAssignmentStatusUpdateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        scheduleService.updateAssignmentStatus(
                userId, targetScheduleId, request.excludedScheduleId());

        return ResponseEntity.noContent().build();
    }
}
