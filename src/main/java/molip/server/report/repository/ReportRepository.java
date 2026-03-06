package molip.server.report.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import molip.server.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByUserIdAndStartDateAndEndDate(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query(
            "select r from Report r "
                    + "join fetch r.user u "
                    + "where r.startDate = :startDate "
                    + "and r.endDate = :endDate "
                    + "and r.deletedAt is null "
                    + "and u.deletedAt is null")
    List<Report> findWeeklyTargets(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
