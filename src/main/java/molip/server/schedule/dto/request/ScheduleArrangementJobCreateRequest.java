package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 배치 작업 생성 요청")
public record ScheduleArrangementJobCreateRequest(
        @Schema(description = "작업 목록") List<ScheduleArrangementTaskRequest> tasks) {}
