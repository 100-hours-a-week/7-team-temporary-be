package molip.server.report.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.entity.Report;
import molip.server.report.repository.ReportRepository;
import molip.server.user.entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public Report findByUserIdAndPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        return reportRepository
                .findByUserIdAndStartDateAndEndDate(userId, startDate, endDate)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Report getReport(Long userId, LocalDate startDate, LocalDate endDate) {
        Report report = findByUserIdAndPeriod(userId, startDate, endDate);

        if (report == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND);
        }

        return report;
    }

    @Transactional
    public Report getOrCreateReport(Users user, LocalDate startDate, LocalDate endDate) {
        Report report = findByUserIdAndPeriod(user.getId(), startDate, endDate);

        if (report != null) {
            return report;
        }

        return reportRepository.save(new Report(user, startDate, endDate, 2, 0));
    }
}
