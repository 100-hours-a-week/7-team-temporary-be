package molip.server.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

@Schema(description = "일정 배정(일자 이동) 요청")
public record ScheduleDayPlanAssignRequest(
        @Schema(description = "대상 일자 플랜 ID", example = "10") Long targetDayPlanId,
        @Schema(description = "시작 시간", example = "15:00") @JsonFormat(pattern = "HH:mm")
                LocalTime startAt,
        @Schema(description = "종료 시간", example = "16:30") @JsonFormat(pattern = "HH:mm")
                LocalTime endAt) {}
