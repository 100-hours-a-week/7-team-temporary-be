package molip.server.schedule.event;

import molip.server.schedule.enums.ScheduleActionType;

public record ScheduleActionLoggedEvent(
        Long userId, Long scheduleId, ScheduleActionType actionType, String apiPath) {}
