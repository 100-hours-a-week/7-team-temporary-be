package molip.server.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "리포트 응답")
public record ReportResponse(
    @Schema(description = "리포트 ID", example = "20") Long reportId,
    @Schema(description = "시작일", example = "2026-01-12") String startDate,
    @Schema(description = "종료일", example = "2026-01-18") String endDate,
    @Schema(description = "AI 응답 제한", example = "10") int aiReportResponseLimit,
    @Schema(description = "AI 응답 사용량", example = "1") int aiReportResponseUsed,
    @Schema(description = "일자별 통계") List<ReportDailyStatResponse> dailyStats) {}
