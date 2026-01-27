package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 생성 응답")
public record ScheduleCreateResponse(
        @Schema(description = "일정 ID", example = "101") Long scheduleId) {

    public static ScheduleCreateResponse from(molip.server.schedule.entity.Schedule schedule) {
        return new ScheduleCreateResponse(schedule.getId());
    }
}
