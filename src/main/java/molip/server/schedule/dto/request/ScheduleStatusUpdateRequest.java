package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.ScheduleStatus;

@Schema(description = "일정 상태 변경 요청")
public record ScheduleStatusUpdateRequest(
    @Schema(description = "상태", example = "TODO") ScheduleStatus status) {}
