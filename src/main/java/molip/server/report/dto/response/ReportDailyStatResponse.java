package molip.server.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일자 성취 통계")
public record ReportDailyStatResponse(
    @Schema(description = "일자", example = "2026-01-12") String date,
    @Schema(description = "성취율", example = "80") int achievementRate) {}
