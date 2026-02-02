package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "부모 일정별 자식 일정 생성 요청")
public record ScheduleChildrenCreateItem(
        @Schema(description = "부모 일정 ID", example = "3") Long parentScheduleId,
        @Schema(description = "자식 일정 제목 목록", example = "[\"API 설계하기\", \"ERD짜기\"]")
                List<String> titles) {}
