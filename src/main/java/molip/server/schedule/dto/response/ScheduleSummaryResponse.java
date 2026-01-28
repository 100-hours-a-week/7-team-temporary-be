package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.enums.ScheduleType;
import molip.server.schedule.entity.Schedule;

@Schema(description = "일정 항목")
public record ScheduleSummaryResponse(
        @Schema(description = "일정 ID", example = "99") Long scheduleId,
        @Schema(description = "부모 제목", example = "수업") String parentTitle,
        @Schema(description = "제목", example = "수업(고정)") String title,
        @Schema(description = "상태", example = "TODO") ScheduleStatus status,
        @Schema(description = "타입", example = "FIXED") ScheduleType type,
        @Schema(description = "배정 주체", example = "USER") AssignedBy assignedBy,
        @Schema(description = "배정 상태", example = "FIXED") AssignmentStatus assignmentStatus,
        @Schema(description = "시작 시간", example = "13:00") String startAt,
        @Schema(description = "종료 시간", example = "14:30") String endAt,
        @Schema(description = "예상 소요 시간", example = "HOUR_1_TO_2")
                EstimatedTimeRange estimatedTimeRange,
        @Schema(description = "집중도", example = "4") Integer focusLevel,
        @Schema(description = "긴급 여부", example = "true") Boolean isUrgent) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ScheduleSummaryResponse from(Schedule schedule, Schedule parentSchedule) {

        return new ScheduleSummaryResponse(
                schedule.getId(),
                parentSchedule == null ? null : parentSchedule.getTitle(),
                schedule.getTitle(),
                schedule.getStatus(),
                schedule.getType(),
                schedule.getAssignedBy(),
                schedule.getAssignmentStatus(),
                formatTime(schedule.getStartAt()),
                formatTime(schedule.getEndAt()),
                schedule.getEstimatedTimeRange(),
                schedule.getFocusLevel(),
                schedule.getIsUrgent());
    }

    private static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }
}
