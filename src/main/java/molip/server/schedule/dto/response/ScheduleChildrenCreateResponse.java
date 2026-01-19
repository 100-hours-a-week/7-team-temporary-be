package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "자식 일정 생성 응답")
public record ScheduleChildrenCreateResponse(
    @Schema(description = "자식 일정 목록") List<ScheduleChildResponse> children) {}
