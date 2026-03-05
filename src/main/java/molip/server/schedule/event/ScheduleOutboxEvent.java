package molip.server.schedule.event;

import molip.server.migration.event.ChangeType;
import molip.server.schedule.entity.Schedule;

public record ScheduleOutboxEvent(Schedule schedule, ChangeType changeType) {}
