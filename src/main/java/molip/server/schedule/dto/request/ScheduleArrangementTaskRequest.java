package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;

@Schema(description = "AI 배치 작업 요청 항목")
public record ScheduleArrangementTaskRequest(
        @Schema(description = "일정 ID", example = "3") Long scheduleId,
        @Schema(description = "일자 플랜 ID", example = "10") Long dayPlanId,
        @Schema(description = "부모 일정 ID", example = "1") Long parentScheduleId,
        @Schema(description = "제목", example = "알고리즘 문제 풀기") String title,
        @Schema(description = "타입", example = "FLEX") ScheduleType type,
        @Schema(description = "배정 주체", example = "USER") AssignedBy assignedBy,
        @Schema(description = "배정 상태", example = "NOT_ASSIGNED") AssignmentStatus assignmentStatus,
        @Schema(description = "시작 시간", example = "13:00") String startAt,
        @Schema(description = "종료 시간", example = "14:30") String endAt,
        @Schema(description = "예상 소요 시간", example = "HOUR_1_TO_2")
                EstimatedTimeRange estimatedTimeRange,
        @Schema(description = "집중도", example = "4") Integer focusLevel,
        @Schema(description = "긴급 여부", example = "true") Boolean isUrgent) {}
