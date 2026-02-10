package molip.server.schedule.repository;

import molip.server.common.enums.ScheduleStatus;

public interface ScheduleStatusCount {

    ScheduleStatus getStatus();

    long getCount();
}
