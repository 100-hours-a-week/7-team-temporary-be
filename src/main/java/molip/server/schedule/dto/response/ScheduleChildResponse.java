package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자식 일정 응답")
public record ScheduleChildResponse(
    @Schema(description = "일정 ID", example = "101") Long scheduleId,
    @Schema(description = "제목", example = "API 설계하기") String title) {}
