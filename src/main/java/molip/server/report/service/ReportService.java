package molip.server.report.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
    public Report getReportWithUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        Report report = findByUserIdAndPeriod(userId, startDate, endDate);

        if (report == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND);
        }

        return report;
    }

    @Transactional(readOnly = true)
    public Report getReportWithUserId(Long userId, Long reportId) {
        if (reportId == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        Report report =
                reportRepository
                        .findById(reportId)
                        .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC));

        if (!report.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REPORT_ACCESS);
        }

        return report;
    }

    @Transactional(readOnly = true)
    public Report getReportWithUserId(Long reportId) {
        if (reportId == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        return reportRepository
                .findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC));
    }

    @Transactional
    public Report getOrCreateReport(Users user, LocalDate startDate, LocalDate endDate) {
        validateReportStartDateAfterSignup(user, startDate);

        Report report = findByUserIdAndPeriod(user.getId(), startDate, endDate);

        if (report != null) {
            return report;
        }

        return reportRepository.save(new Report(user, startDate, endDate, 2, 0));
    }

    private void validateReportStartDateAfterSignup(Users user, LocalDate startDate) {
        if (user.getCreatedAt() == null) {
            return;
        }

        LocalDate firstAvailableStartDate =
                user.getCreatedAt()
                        .toLocalDate()
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        if (startDate.isBefore(firstAvailableStartDate)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REPORT_BEFORE_SIGNUP);
        }
    }
}
