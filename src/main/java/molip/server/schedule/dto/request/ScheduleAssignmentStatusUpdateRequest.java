package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 배정 상태 변경 요청")
public record ScheduleAssignmentStatusUpdateRequest(
        @Schema(description = "제외된 일정 ID", example = "3") Long excludedScheduleId) {}
