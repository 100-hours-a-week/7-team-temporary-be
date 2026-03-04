package molip.server.report.repository;

import java.util.List;
import molip.server.report.entity.ReportDailyStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportDailyStatRepository extends JpaRepository<ReportDailyStat, Long> {

    List<ReportDailyStat> findByReportIdOrderByReportDateAsc(Long reportId);

    void deleteByReportId(Long reportId);
}
