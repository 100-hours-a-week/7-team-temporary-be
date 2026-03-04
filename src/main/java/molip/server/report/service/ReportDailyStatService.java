package molip.server.report.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportDailyStat;
import molip.server.report.repository.ReportDailyStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportDailyStatService {

    private final ReportDailyStatRepository reportDailyStatRepository;

    @Transactional(readOnly = true)
    public List<ReportDailyStat> getDailyStats(Long reportId) {
        return reportDailyStatRepository.findByReportIdOrderByStatDateAsc(reportId);
    }

    @Transactional
    public List<ReportDailyStat> replaceDailyStats(
            Report report, List<ReportDailyStat> dailyStats) {
        reportDailyStatRepository.deleteByReportId(report.getId());

        if (dailyStats == null || dailyStats.isEmpty()) {
            return List.of();
        }

        return reportDailyStatRepository.saveAll(new ArrayList<>(dailyStats));
    }
}
