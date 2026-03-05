package molip.server.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리포트 메시지 스트림 재개 응답")
public record ReportMessageStreamResumeResponse(
        @Schema(description = "리포트 ID", example = "20") Long reportId,
        @Schema(description = "입력 메시지 ID", example = "100") Long inputMessageId,
        @Schema(description = "스트림 메시지 ID", example = "101") Long streamMessageId,
        @Schema(description = "현재 상태", example = "GENERATING") String status,
        @Schema(description = "누적 텍스트", example = "다음 주는 저녁 몰입 시간대에 ...") String content) {

    public static ReportMessageStreamResumeResponse of(
            Long reportId,
            Long inputMessageId,
            Long streamMessageId,
            String status,
            String content) {
        return new ReportMessageStreamResumeResponse(
                reportId, inputMessageId, streamMessageId, status, content);
    }
}
