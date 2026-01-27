package molip.server.schedule.repository;

import java.util.Optional;
import molip.server.schedule.entity.DayPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayPlanRepository extends JpaRepository<DayPlan, Long> {
    Optional<DayPlan> findByIdAndDeletedAtIsNull(Long id);

    Optional<DayPlan> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
