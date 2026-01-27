package molip.server.schedule.repository;

import java.time.LocalTime;
import java.util.Optional;
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
            @Param("startAt") LocalTime startAt,
            @Param("endAt") LocalTime endAt);

    @Query(
            "select count(s) > 0 from Schedule s "
                    + "where s.dayPlan.id = :dayPlanId and s.deletedAt is null "
                    + "and s.startAt is not null and s.endAt is not null "
                    + "and s.startAt < :endAt and s.endAt > :startAt "
                    + "and s.id <> :scheduleId")
    boolean existsTimeOverlapExcludingId(
            @Param("dayPlanId") Long dayPlanId,
            @Param("scheduleId") Long scheduleId,
            @Param("startAt") LocalTime startAt,
            @Param("endAt") LocalTime endAt);

    @Query(
            "select s from Schedule s "
                    + "join fetch s.dayPlan dp "
                    + "join fetch dp.user u "
                    + "where s.id = :id and s.deletedAt is null")
    Optional<Schedule> findByIdWithDayPlanUser(@Param("id") Long id);

    boolean existsByParentScheduleIdAndDeletedAtIsNull(Long parentScheduleId);

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
