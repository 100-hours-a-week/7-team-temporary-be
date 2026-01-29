package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 배치 결과 응답")
public record ScheduleArrangeResponse(
        @Schema(description = "결과 목록") List<ScheduleArrangeResultResponse> results) {}
