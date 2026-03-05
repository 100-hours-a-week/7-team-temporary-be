package molip.server.schedule.repository;

import molip.server.schedule.entity.ScheduleActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleActionLogRepository extends JpaRepository<ScheduleActionLog, Long> {}
