package molip.server.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
        description = "자식 일정 생성 요청",
        example =
                "{\n"
                        + "  \"schedules\": [\n"
                        + "    {\n"
                        + "      \"parentScheduleId\": 3,\n"
                        + "      \"titles\": [\"API 설계하기\", \"ERD짜기\"]\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"parentScheduleId\": 5,\n"
                        + "      \"titles\": [\"API 문서화\", \"테스트 작성\"]\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}")
public record ScheduleChildrenCreateRequest(
        @Schema(description = "부모 일정별 자식 생성 요청 목록") List<ScheduleChildrenCreateItem> schedules) {}
