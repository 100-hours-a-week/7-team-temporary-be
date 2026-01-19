package molip.server.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.common.response.CursorResponse;
import molip.server.common.response.ServerResponse;
import molip.server.report.dto.request.ReportMessageCreateRequest;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.dto.response.ReportMessageItemResponse;
import molip.server.report.dto.response.ReportResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Report", description = "리포트 API")
@RestController
@RequestMapping("/reports")
public class ReportController {
  @Operation(summary = "특정 주간 리포트 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ReportResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "날짜 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "리포트 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping
  public ResponseEntity<ServerResponse<ReportResponse>> getReportByStartDate(
      @RequestParam String startDate) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "리포트 메시지 목록 조회")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = CursorResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "페이지 정보 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "리포트 접근 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "리포트 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping("/{reportId}/messages")
  public ResponseEntity<ServerResponse<CursorResponse<ReportMessageItemResponse>>>
      getReportMessages(
          @PathVariable Long reportId,
          @RequestParam(required = false) Long cursor,
          @RequestParam(required = false, defaultValue = "20") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "AI 응답 생성을 위한 메시지 전송")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "등록 성공",
        content = @Content(schema = @Schema(implementation = ReportMessageCreateResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "inputMessage 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "리포트 접근 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "리포트 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "응답 생성 진행 중",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @PostMapping("/{reportId}/message")
  public ResponseEntity<ServerResponse<ReportMessageCreateResponse>> createReportMessage(
      @PathVariable Long reportId, @RequestBody ReportMessageCreateRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "리포트 메시지 스트림 구독")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "스트림 시작"),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "리포트 접근 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "메시지 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 종료된 스트림",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @GetMapping(
      value = "/{reportId}/message/{messageId}",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> streamReportMessage(
      @PathVariable Long reportId, @PathVariable Long messageId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "응답 생성 취소")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "취소 성공"),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "리포트 접근 권한 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "메시지 없음",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 종료된 응답 생성",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping("/{reportId}/message/{messageId}")
  public ResponseEntity<Void> cancelReportMessage(
      @PathVariable Long reportId, @PathVariable Long messageId) {
    return ResponseEntity.noContent().build();
  }
}