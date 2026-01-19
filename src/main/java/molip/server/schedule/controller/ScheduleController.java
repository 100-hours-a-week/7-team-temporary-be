package molip.server.schedule.controller;

import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.schedule.dto.request.ScheduleArrangementJobCreateRequest;
import molip.server.schedule.dto.request.ScheduleAssignmentStatusUpdateRequest;
import molip.server.schedule.dto.request.ScheduleChildrenCreateRequest;
import molip.server.schedule.dto.request.ScheduleCreateRequest;
import molip.server.schedule.dto.request.ScheduleStatusUpdateRequest;
import molip.server.schedule.dto.response.DayPlanScheduleListResponse;
import molip.server.schedule.dto.response.ScheduleArrangementJobResponse;
import molip.server.schedule.dto.response.ScheduleChildrenCreateResponse;
import molip.server.schedule.dto.response.ScheduleCreateResponse;
import molip.server.schedule.dto.response.ScheduleItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ScheduleController implements ScheduleApi {
  @PostMapping("/day-plan/{dayPlanId}/schedule")
  @Override
  public ResponseEntity<ServerResponse<ScheduleCreateResponse>> createSchedule(
      @PathVariable Long dayPlanId, @RequestBody ScheduleCreateRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/day-plan/{dayPlanId}/schedule")
  @Override
  public ResponseEntity<ServerResponse<PageResponse<ScheduleItemResponse>>> getDayPlanSchedules(
      @PathVariable Long dayPlanId,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @GetMapping("/day-plan/schedule")
  @Override
  public ResponseEntity<ServerResponse<DayPlanScheduleListResponse>> getDaySchedulesByDate(
      @RequestParam String date,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @PutMapping("/schedule/{scheduleId}")
  @Override
  public ResponseEntity<Void> updateSchedule(
      @PathVariable Long scheduleId, @RequestBody ScheduleCreateRequest request) {
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
      @PathVariable Long dayPlanId, @RequestBody ScheduleArrangementJobCreateRequest request) {
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
  public ResponseEntity<ServerResponse<PageResponse<ScheduleItemResponse>>> getExcludedSchedules(
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
