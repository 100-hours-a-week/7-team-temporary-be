package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "자식 일정 생성 요청")
public record ScheduleChildrenCreateRequest(
    @Schema(description = "자식 일정 제목 목록", example = "[\"API 설계하기\", \"ERD짜기\"]")
        List<String> titles) {}
