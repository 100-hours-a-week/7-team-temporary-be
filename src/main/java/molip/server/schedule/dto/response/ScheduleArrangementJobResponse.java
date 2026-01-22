package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.ArrangementJobState;

@Schema(description = "AI 배치 작업 응답")
public record ScheduleArrangementJobResponse(
        @Schema(description = "작업 ID", example = "12345") Long jobId,
        @Schema(description = "일자 플랜 ID", example = "10") Long dayPlanId,
        @Schema(description = "작업 상태", example = "QUEUED") ArrangementJobState state) {}
