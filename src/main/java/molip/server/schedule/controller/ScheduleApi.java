package molip.server.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Schedule", description = "일정 API")
public interface ScheduleApi {

    @Operation(summary = "일정 생성")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content =
                        @Content(schema = @Schema(implementation = ScheduleCreateResponse.class))),
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
    ResponseEntity<ServerResponse<ScheduleCreateResponse>> createSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            Long dayPlanId,
            ScheduleCreateRequest request);

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
    ResponseEntity<ServerResponse<PageResponse<ScheduleItemResponse>>> getDayPlanSchedules(
            Long dayPlanId, int page, int size);

    @Operation(summary = "특정 일자 일정 전체 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                implementation =
                                                        DayPlanScheduleListResponse.class))),
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
    ResponseEntity<ServerResponse<DayPlanScheduleListResponse>> getDaySchedulesByDate(
            String date, int page, int size);

    @Operation(summary = "일정 정보 수정")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
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
    ResponseEntity<Void> updateSchedule(Long scheduleId, ScheduleCreateRequest request);

    @Operation(summary = "일정 삭제")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
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
    ResponseEntity<Void> deleteSchedule(Long scheduleId);

    @Operation(summary = "일정 AI 배치 Job 생성")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "202",
                description = "작업 생성",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                implementation =
                                                        ScheduleArrangementJobResponse.class))),
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
    ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> createArrangementJob(
            Long dayPlanId, ScheduleArrangementJobCreateRequest request);

    @Operation(summary = "일정 AI 배치 Job 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                implementation =
                                                        ScheduleArrangementJobResponse.class))),
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
    ResponseEntity<ServerResponse<ScheduleArrangementJobResponse>> getArrangementJob(Long jobId);

    @Operation(summary = "자식 일정 생성")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                implementation =
                                                        ScheduleChildrenCreateResponse.class))),
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
    ResponseEntity<ServerResponse<ScheduleChildrenCreateResponse>> createChildren(
            Long scheduleId, ScheduleChildrenCreateRequest request);

    @Operation(summary = "일정 처리 상태 변경")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "상태 변경 성공"),
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
    ResponseEntity<Void> updateStatus(Long scheduleId, ScheduleStatusUpdateRequest request);

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
    ResponseEntity<ServerResponse<PageResponse<ScheduleItemResponse>>> getExcludedSchedules(
            String status, int page, int size);

    @Operation(summary = "일정 배정 상태 변경")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "상태 변경 성공"),
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
    ResponseEntity<Void> updateAssignmentStatus(
            Long scheduleId, ScheduleAssignmentStatusUpdateRequest request);
}
