package molip.server.schedule.repository;

import molip.server.schedule.entity.ScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleHistoryRepository extends JpaRepository<ScheduleHistory, Long> {}
