package molip.server.schedule.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import molip.server.common.enums.AssignmentStatus;
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

    @Query(
            "select s from Schedule s "
                    + "join fetch s.dayPlan dp "
                    + "join fetch dp.user u "
                    + "where s.id = :id")
    Optional<Schedule> findByIdWithDayPlanUserIncludeDeleted(@Param("id") Long id);

    boolean existsByParentScheduleIdAndDeletedAtIsNull(Long parentScheduleId);

    @Query(
            value =
                    "select s from Schedule s "
                            + "left join fetch s.parentSchedule "
                            + "where s.dayPlan.id = :dayPlanId "
                            + "and s.deletedAt is null "
                            + "and s.startAt is not null "
                            + "and s.assignmentStatus <> AssignmentStatus.EXCLUDED",
            countQuery =
                    "select count(s) from Schedule s "
                            + "where s.dayPlan.id = :dayPlanId "
                            + "and s.deletedAt is null "
                            + "and s.startAt is not null "
                            + "and s.assignmentStatus <> AssignmentStatus.EXCLUDED")
    Page<Schedule> findTimeAssignedByDayPlanId(
            @Param("dayPlanId") Long dayPlanId, Pageable pageable);

    @Query(
            value =
                    "select s from Schedule s "
                            + "left join fetch s.parentSchedule "
                            + "join s.dayPlan dp "
                            + "join dp.user u "
                            + "where u.id = :userId "
                            + "and s.deletedAt is null "
                            + "and s.assignmentStatus = :status "
                            + "and dp.planDate between :fromDate and :toDate",
            countQuery =
                    "select count(s) from Schedule s "
                            + "join s.dayPlan dp "
                            + "join dp.user u "
                            + "where u.id = :userId "
                            + "and s.deletedAt is null "
                            + "and s.assignmentStatus = :status "
                            + "and dp.planDate between :fromDate and :toDate")
    Page<Schedule> findExcludedSchedulesByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("status") AssignmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);
}
