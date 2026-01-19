package molip.server.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리포트 메시지 생성 요청")
public record ReportMessageCreateRequest(
    @Schema(description = "입력 메시지", example = "this is text") String inputMessage) {}
