package molip.server.report.repository;

import molip.server.report.entity.ReportDailyStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportDailyStatRepository extends JpaRepository<ReportDailyStat, Long> {

    void deleteByReportId(Long reportId);
}
