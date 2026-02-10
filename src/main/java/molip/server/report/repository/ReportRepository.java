package molip.server.report.repository;

import java.time.LocalDate;
import java.util.Optional;
import molip.server.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByUserIdAndStartDateAndEndDate(
            Long userId, LocalDate startDate, LocalDate endDate);
}
