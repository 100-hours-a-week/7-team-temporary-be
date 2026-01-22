package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "일자 일정 목록 응답")
public record DayPlanScheduleListResponse(
        @Schema(description = "일자 플랜 ID", example = "2") Long dayPlanId,
        @Schema(description = "일정 목록") List<ScheduleItemResponse> content,
        @Schema(description = "페이지", example = "1") int page,
        @Schema(description = "페이지 크기", example = "10") int size,
        @Schema(description = "전체 요소 수", example = "8") long totalElements,
        @Schema(description = "전체 페이지 수", example = "1") int totalPages) {}
