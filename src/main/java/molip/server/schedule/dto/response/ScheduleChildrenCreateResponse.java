package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import molip.server.schedule.entity.Schedule;

@Schema(description = "자식 일정 생성 응답")
public record ScheduleChildrenCreateResponse(
        @Schema(description = "자식 일정 목록") List<ScheduleChildResponse> children) {

    public static ScheduleChildrenCreateResponse from(List<Schedule> schedules) {

        List<ScheduleChildResponse> children =
                schedules.stream().map(ScheduleChildResponse::from).toList();

        return new ScheduleChildrenCreateResponse(children);
    }
}
