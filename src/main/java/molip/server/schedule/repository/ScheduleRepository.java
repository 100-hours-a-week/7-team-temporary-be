package molip.server.schedule.repository;

import java.time.LocalTime;
import molip.server.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query(
            "select count(s) > 0 from Schedule s "
                    + "where s.dayPlan.id = :dayPlanId and s.deletedAt is null "
                    + "and s.startAt is not null and s.endAt is not null "
                    + "and s.startAt < :endAt and s.endAt > :startAt")
    boolean existsTimeOverlap(
            @Param("dayPlanId") Long dayPlanId,
            @Param("startAt") LocalTime startAt,
            @Param("endAt") LocalTime endAt);
}
