package molip.server.schedule.repository;

import java.time.LocalDateTime;
import molip.server.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    @Query(
            value =
                    "select s from Schedule s "
                            + "left join fetch s.parentSchedule "
                            + "where s.dayPlan.id = :dayPlanId "
                            + "and s.deletedAt is null "
                            + "and s.startAt is not null",
            countQuery =
                    "select count(s) from Schedule s "
                            + "where s.dayPlan.id = :dayPlanId "
                            + "and s.deletedAt is null "
                            + "and s.startAt is not null")
    Page<Schedule> findTimeAssignedByDayPlanId(
            @Param("dayPlanId") Long dayPlanId, Pageable pageable);
}
