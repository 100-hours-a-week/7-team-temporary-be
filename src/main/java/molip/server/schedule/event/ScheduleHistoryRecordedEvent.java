package molip.server.schedule.event;

import molip.server.schedule.entity.ScheduleHistory;

public record ScheduleHistoryRecordedEvent(ScheduleHistory history) {}
