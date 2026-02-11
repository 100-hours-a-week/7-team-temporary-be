package molip.server.reflection.repository;

import molip.server.reflection.entity.DayReflection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayReflectionRepository extends JpaRepository<DayReflection, Long> {

    boolean existsByDayPlanIdAndDeletedAtIsNull(Long dayPlanId);
}
