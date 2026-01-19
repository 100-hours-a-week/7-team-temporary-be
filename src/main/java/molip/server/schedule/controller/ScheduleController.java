package molip.server.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Schedule", description = "일정 API")
@RestController
@RequestMapping
public class ScheduleController {
  @Operation(summary = "일정 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "생성 성공",
        content = @Content(schema = @Schema(implementation = ScheduleCreateResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "시간 충돌",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/day-plan/{dayPlanId}/schedule")
  public ResponseEntity<ServerResponse<ScheduleCreateResponse>> createSchedule(
      @PathVariable Long dayPlanId, @RequestBody ScheduleCreateRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "특정 일자 일정 TodoList 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "일자 플랜 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/day-plan/{dayPlanId}/schedule")
  public ResponseEntity<ServerResponse<PageResponse<ScheduleItemResponse>>> getDayPlanSchedules(
      @PathVariable Long dayPlanId,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "특정 일자 일정 전체 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = DayPlanScheduleListResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "일자 플랜 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/day-plan/schedule")
  public ResponseEntity<ServerResponse<DayPlanScheduleListResponse>> getDaySchedulesByDate(
      @RequestParam String date,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "일정 정보 수정")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "수정 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "시간 충돌",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PutMapping("/schedule/{scheduleId}")
  public ResponseEntity<Void> updateSchedule(
      @PathVariable Long scheduleId, @RequestBody ScheduleCreateRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "일정 삭제")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "삭제 성공"),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "삭제 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "일정 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 삭제된 일정",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping("/schedule/{scheduleId}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "일정 AI 배치 Job 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "202",
        description = "작업 생성",
        content =
            @Content(schema = @Schema(implementation = ScheduleArrangementJobResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "DayPlan 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 작업 진행 중",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/day-plan/{dayPlanId}/schedule/arrangement-jobs")
  public ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> createArrangementJob(
      @PathVariable Long dayPlanId, @RequestBody ScheduleArrangementJobCreateRequest request) {
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
  }

  @Operation(summary = "일정 AI 배치 Job 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(schema = @Schema(implementation = ScheduleArrangementJobResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "410",
        description = "조회 기간 만료",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/schedule/arrangement-jobs/{jobId}")
  public ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> getArrangementJob(
      @PathVariable Long jobId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "자식 일정 생성")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "생성 성공",
        content =
            @Content(schema = @Schema(implementation = ScheduleChildrenCreateResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "자식 일정 조건 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "부모 일정 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 자식 일정 존재",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/schedule/{scheduleId}/children")
  public ResponseEntity<ServerResponse<ScheduleChildrenCreateResponse>> createChildren(
      @PathVariable Long scheduleId, @RequestBody ScheduleChildrenCreateRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "일정 처리 상태 변경")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "상태 변경 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "요청 상태 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "일정 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping("/schedule/{scheduleId}/status")
  public ResponseEntity<Void> updateStatus(
      @PathVariable Long scheduleId, @RequestBody ScheduleStatusUpdateRequest request) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "제외된 일정 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = PageResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/schedules")
  public ResponseEntity<ServerResponse<PageResponse<ScheduleItemResponse>>> getExcludedSchedules(
      @RequestParam String status,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "일정 배정 상태 변경")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "상태 변경 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "요청 상태 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "일정 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PatchMapping("/schedule/{scheduleId}/assignment-status")
  public ResponseEntity<Void> updateAssignmentStatus(
      @PathVariable Long scheduleId, @RequestBody ScheduleAssignmentStatusUpdateRequest request) {
    return ResponseEntity.noContent().build();
  }
}