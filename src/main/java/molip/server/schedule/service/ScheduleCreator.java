package molip.server.schedule.service;

import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;

public interface ScheduleCreator {
    ScheduleType supports();

    Schedule create(
            DayPlan dayPlan,
            String title,
            String startAt,
            String endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent);
}
