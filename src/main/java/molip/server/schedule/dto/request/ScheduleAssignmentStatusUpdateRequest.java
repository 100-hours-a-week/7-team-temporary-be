package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.AssignmentStatus;

@Schema(description = "일정 배정 상태 변경 요청")
public record ScheduleAssignmentStatusUpdateRequest(
        @Schema(description = "배정 상태", example = "ASSIGNED") AssignmentStatus assignmentStatus) {}
