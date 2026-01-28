package molip.server.schedule.service.strategy;

import java.time.LocalTime;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;

public interface ScheduleCreator {
    ScheduleType supports();

    Schedule create(
            DayPlan dayPlan,
            String title,
            LocalTime startAt,
            LocalTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent);
}
