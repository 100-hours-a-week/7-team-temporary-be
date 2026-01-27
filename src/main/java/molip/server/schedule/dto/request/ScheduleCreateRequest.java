package molip.server.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;

@Schema(description = "일정 생성/수정 요청")
public record ScheduleCreateRequest(
        @Schema(description = "제목", example = "제목") String title,
        @Schema(description = "타입", example = "FLEX") ScheduleType type,
        @Schema(description = "시작 시간", example = "13:00") @JsonFormat(pattern = "HH:mm")
                LocalTime startAt,
        @Schema(description = "종료 시간", example = "14:30") @JsonFormat(pattern = "HH:mm")
                LocalTime endAt,
        @Schema(description = "예상 소요 시간", example = "HOUR_1_TO_2")
                EstimatedTimeRange estimatedTimeRange,
        @Schema(description = "집중도", example = "8") Integer focusLevel,
        @Schema(description = "긴급 여부", example = "true") Boolean isUrgent) {}
