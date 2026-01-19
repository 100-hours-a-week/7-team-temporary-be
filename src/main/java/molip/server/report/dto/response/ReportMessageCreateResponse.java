package molip.server.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리포트 메시지 생성 응답")
public record ReportMessageCreateResponse(
    @Schema(description = "입력 메시지 ID", example = "100") Long inputMessageId,
    @Schema(description = "스트림 메시지 ID", example = "101") Long streamMessageId) {}
