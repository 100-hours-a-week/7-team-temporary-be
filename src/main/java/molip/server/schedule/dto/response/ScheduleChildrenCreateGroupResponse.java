package molip.server.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import molip.server.schedule.entity.Schedule;

@Schema(description = "부모 일정별 자식 일정 생성 결과")
public record ScheduleChildrenCreateGroupResponse(
        @Schema(description = "부모 일정 ID", example = "3") Long parentScheduleId,
        @Schema(description = "자식 일정 목록") List<ScheduleChildResponse> children) {

    public static ScheduleChildrenCreateGroupResponse of(
            Long parentScheduleId, List<ScheduleChildResponse> children) {

        return new ScheduleChildrenCreateGroupResponse(parentScheduleId, children);
    }

    public static List<ScheduleChildrenCreateGroupResponse> groupFrom(List<Schedule> children) {

        LinkedHashMap<Long, List<ScheduleChildResponse>> grouped = new LinkedHashMap<>();

        for (Schedule child : children) {

            Long parentScheduleId = child.getParentSchedule().getId();
            grouped.computeIfAbsent(parentScheduleId, key -> new ArrayList<>())
                    .add(ScheduleChildResponse.from(child));
        }

        List<ScheduleChildrenCreateGroupResponse> response = new ArrayList<>();
        for (Map.Entry<Long, List<ScheduleChildResponse>> entry : grouped.entrySet()) {

            response.add(ScheduleChildrenCreateGroupResponse.of(entry.getKey(), entry.getValue()));
        }

        return response;
    }
}
