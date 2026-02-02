package molip.server.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.schedule.dto.request.ScheduleArrangementJobCreateRequest;
import molip.server.schedule.dto.request.ScheduleAssignmentStatusUpdateRequest;
import molip.server.schedule.dto.request.ScheduleChildrenCreateRequest;
import molip.server.schedule.dto.request.ScheduleCreateRequest;
import molip.server.schedule.dto.request.ScheduleDayPlanAssignRequest;
import molip.server.schedule.dto.request.ScheduleStatusUpdateRequest;
import molip.server.schedule.dto.request.ScheduleUpdateRequest;
import molip.server.schedule.dto.response.DayPlanSchedulePageResponse;
import molip.server.schedule.dto.response.DayPlanTodoListResponse;
import molip.server.schedule.dto.response.ScheduleArrangeResponse;
import molip.server.schedule.dto.response.ScheduleArrangementJobResponse;
import molip.server.schedule.dto.response.ScheduleChildrenCreateGroupResponse;
import molip.server.schedule.dto.response.ScheduleCreateResponse;
import molip.server.schedule.dto.response.ScheduleSummaryResponse;
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
                content =
                        @Content(schema = @Schema(implementation = DayPlanTodoListResponse.class))),
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
    ResponseEntity<ServerResponse<DayPlanTodoListResponse>> getAllSchedulesByDayPlan(
            @AuthenticationPrincipal UserDetails userDetails, Long dayPlanId, int page, int size);

    @Operation(summary = "특정 일자 일정 전체 조회")
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
    ResponseEntity<ServerResponse<DayPlanSchedulePageResponse>> getOnlyTimeAssignedSchedulesByDate(
            @AuthenticationPrincipal UserDetails userDetails, String date, int page, int size);

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
    ResponseEntity<Void> updateSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            Long scheduleId,
            ScheduleUpdateRequest request);

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
    ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal UserDetails userDetails, Long scheduleId);

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
                                schema = @Schema(implementation = ServerResponse.class),
                                examples =
                                        @ExampleObject(
                                                value =
                                                        "{\n"
                                                                + "  \"status\": \"SUCCESS\",\n"
                                                                + "  \"message\": \"자식 일정이 생성되었습니다.\",\n"
                                                                + "  \"data\": [\n"
                                                                + "    {\n"
                                                                + "      \"parentScheduleId\": 3,\n"
                                                                + "      \"children\": [\n"
                                                                + "        {\"scheduleId\": 101, \"title\": \"API 설계하기\"},\n"
                                                                + "        {\"scheduleId\": 102, \"title\": \"ERD짜기\"}\n"
                                                                + "      ]\n"
                                                                + "    },\n"
                                                                + "    {\n"
                                                                + "      \"parentScheduleId\": 5,\n"
                                                                + "      \"children\": [\n"
                                                                + "        {\"scheduleId\": 201, \"title\": \"API 문서화\"},\n"
                                                                + "        {\"scheduleId\": 202, \"title\": \"테스트 작성\"}\n"
                                                                + "      ]\n"
                                                                + "    }\n"
                                                                + "  ]\n"
                                                                + "}"))),
        @ApiResponse(
                responseCode = "400",
                description = "자식 일정 조건 오류/요청 값 오류",
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
    ResponseEntity<ServerResponse<List<ScheduleChildrenCreateGroupResponse>>> createChildren(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(
                            description = "부모 일정별 자식 일정 생성 요청",
                            required = true,
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ScheduleChildrenCreateRequest
                                                                            .class)))
                    ScheduleChildrenCreateRequest request);

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
    ResponseEntity<Void> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            Long scheduleId,
            ScheduleStatusUpdateRequest request);

    @Operation(summary = "제외된 일정 배정(제외된 일정 목록에서 유저가 직접 이동)")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "배정 성공",
                content = @Content(examples = @ExampleObject(value = ""))),
        @ApiResponse(
                responseCode = "400",
                description = "요청 값 오류",
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
                description = "일정/일자 플랜 없음",
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
    ResponseEntity<Void> assignToDayPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            Long scheduleId,
            @RequestBody(
                            description = "일정 배정(일자 이동) 요청",
                            required = true,
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ScheduleDayPlanAssignRequest
                                                                            .class),
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    "{\n"
                                                                            + "  \"targetDayPlanId\": 10,\n"
                                                                            + "  \"startAt\": \"15:00\",\n"
                                                                            + "  \"endAt\": \"16:30\"\n"
                                                                            + "}")))
                    ScheduleDayPlanAssignRequest request);

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
    ResponseEntity<ServerResponse<PageResponse<ScheduleSummaryResponse>>> getExcludedSchedules(
            @AuthenticationPrincipal UserDetails userDetails,
            Long dayPlanId,
            String status,
            int page,
            int size);

    @Operation(summary = "현재 진행 중인 일정 조회")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                schema = @Schema(implementation = ServerResponse.class),
                                examples =
                                        @ExampleObject(
                                                value =
                                                        "{\n"
                                                                + "  \"status\": \"SUCCESS\",\n"
                                                                + "  \"message\": \"현재 일정 조회에 성공했습니다.\",\n"
                                                                + "  \"data\": {\n"
                                                                + "    \"scheduleId\": 3,\n"
                                                                + "    \"parentTitle\": null,\n"
                                                                + "    \"title\": \"알고리즘 문제 풀기\",\n"
                                                                + "    \"status\": \"TODO\",\n"
                                                                + "    \"type\": \"FLEX\",\n"
                                                                + "    \"assignedBy\": \"AI\",\n"
                                                                + "    \"assignmentStatus\": \"ASSIGNED\",\n"
                                                                + "    \"startAt\": \"15:00\",\n"
                                                                + "    \"endAt\": \"16:30\",\n"
                                                                + "    \"estimatedTimeRange\": \"HOUR_1_TO_2\",\n"
                                                                + "    \"focusLevel\": 4,\n"
                                                                + "    \"isUrgent\": true\n"
                                                                + "  }\n"
                                                                + "}"))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<ScheduleSummaryResponse>> getCurrentSchedule(
            @AuthenticationPrincipal UserDetails userDetails);

    @Operation(summary = "AI 일정 배치")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "배치 성공",
                content =
                        @Content(schema = @Schema(implementation = ScheduleArrangeResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 토큰",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "일자 플랜 없음",
                content = @Content(schema = @Schema(implementation = ServerResponse.class))),
        @ApiResponse(
                responseCode = "503",
                description = "AI 엔진 장애/타임아웃",
                content = @Content(schema = @Schema(implementation = ServerResponse.class)))
    })
    ResponseEntity<ServerResponse<ScheduleArrangeResponse>> arrangeSchedules(
            @AuthenticationPrincipal UserDetails userDetails, Long dayPlanId);

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
            @AuthenticationPrincipal UserDetails userDetails,
            Long scheduleId,
            ScheduleAssignmentStatusUpdateRequest request);
}
