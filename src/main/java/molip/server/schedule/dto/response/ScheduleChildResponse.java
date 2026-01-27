package molip.server.schedule.dto.response;

import molip.server.schedule.entity.Schedule;

public record ScheduleChildResponse(Long scheduleId, String title) {

    public static ScheduleChildResponse from(Schedule schedule) {
        return new ScheduleChildResponse(schedule.getId(), schedule.getTitle());
    }
}
