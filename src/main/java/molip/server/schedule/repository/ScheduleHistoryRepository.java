package molip.server.schedule.repository;

import java.util.List;
import molip.server.schedule.entity.ScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleHistoryRepository extends JpaRepository<ScheduleHistory, Long> {

    List<ScheduleHistory> findByScheduleIdInAndDeletedAtIsNull(List<Long> scheduleIds);
}
