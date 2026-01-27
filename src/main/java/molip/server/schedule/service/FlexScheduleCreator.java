package molip.server.schedule.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.enums.ScheduleType;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlexScheduleCreator implements ScheduleCreator {

    @Override
    public ScheduleType supports() {
        return ScheduleType.FLEX;
    }

    @Override
    public Schedule create(
            DayPlan dayPlan,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {

        return Schedule.builder()
                .dayPlan(dayPlan)
                .title(title)
                .status(ScheduleStatus.TODO)
                .type(ScheduleType.FLEX)
                .assignedBy(AssignedBy.USER)
                .assignmentStatus(AssignmentStatus.NOT_ASSIGNED)
                .estimatedTimeRange(estimatedTimeRange)
                .focusLevel(focusLevel)
                .isUrgent(isUrgent)
                .build();
    }
}
