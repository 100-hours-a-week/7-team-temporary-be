package molip.server.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;

@Schema(description = "일정 생성/수정 요청")
public record ScheduleCreateRequest(
        @Schema(description = "제목", example = "제목") String title,
        @Schema(description = "타입", example = "FLEX") ScheduleType type,
        @Schema(description = "시작 시간", example = "2026-01-13T13:00:00")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime startAt,
        @Schema(description = "종료 시간", example = "2026-01-13T14:30:00")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime endAt,
        @Schema(description = "예상 소요 시간", example = "HOUR_1_TO_2")
                EstimatedTimeRange estimatedTimeRange,
        @Schema(description = "집중도", example = "8") Integer focusLevel,
        @Schema(description = "긴급 여부", example = "true") Boolean isUrgent) {}
