package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.ScheduleType;

@Schema(description = "AI 배치 결과 항목")
public record ScheduleArrangeResultResponse(
        @Schema(description = "일정 ID", example = "7") Long scheduleId,
        @Schema(description = "DayPlan ID", example = "100") Long dayPlanId,
        @Schema(description = "제목", example = "프로젝트 기획서 작성") String title,
        @Schema(description = "타입", example = "FLEX") ScheduleType type,
        @Schema(description = "배정 주체", example = "AI") AssignedBy assignedBy,
        @Schema(description = "배정 상태", example = "ASSIGNED") AssignmentStatus assignmentStatus,
        @Schema(description = "시작 시간", example = "16:45") String startAt,
        @Schema(description = "종료 시간", example = "17:30") String endAt) {}
