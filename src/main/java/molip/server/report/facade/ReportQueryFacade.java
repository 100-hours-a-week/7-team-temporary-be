package molip.server.report.facade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportDailyStatResponse;
import molip.server.report.dto.response.ReportResponse;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportDailyStat;
import molip.server.report.service.ReportDailyStatService;
import molip.server.report.service.ReportService;
import molip.server.schedule.service.ScheduleService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportQueryFacade {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ReportService reportService;
    private final ReportDailyStatService reportDailyStatService;
    private final UserService userService;
    private final ScheduleService scheduleService;

    public ReportResponse getReportByStartDate(Long userId, String startDateText) {
        LocalDate startDate = parseStartDate(startDateText);
        LocalDate endDate = startDate.plusDays(6);

        validateReportAvailability(endDate);

        Report report = reportService.findByUserIdAndPeriod(userId, startDate, endDate);

        if (report == null) {
            report = generateWeeklyReport(userId, startDate, endDate);
        }

        List<ReportDailyStatResponse> dailyStats =
                reportDailyStatService.getDailyStats(report.getId()).stream()
                        .map(this::toDailyStatResponse)
                        .toList();

        return ReportResponse.of(report, dailyStats);
    }

    private LocalDate parseStartDate(String startDateText) {
        if (startDateText == null || startDateText.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DATE_REQUIRED);
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateText);

            if (startDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_REPORT_START_DATE);
            }

            return startDate;
        } catch (DateTimeParseException exception) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REPORT_START_DATE);
        }
    }

    private void validateReportAvailability(LocalDate endDate) {
        LocalDateTime availableAt = endDate.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);

        if (now.isBefore(availableAt)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REPORT_NOT_AVAILABLE_YET);
        }
    }

    private ReportDailyStatResponse toDailyStatResponse(ReportDailyStat stat) {
        return ReportDailyStatResponse.of(stat.getStatDate().toString(), stat.getAchievementRate());
    }

    private Report generateWeeklyReport(Long userId, LocalDate startDate, LocalDate endDate) {
        Users user = userService.getUser(userId);

        Report report = reportService.getOrCreateReport(user, startDate, endDate);

        List<ReportDailyStat> dailyStats = new ArrayList<>();
        LocalDate date = startDate;

        for (int i = 0; i < 7; i++) {
            dailyStats.add(
                    new ReportDailyStat(
                            report, date, scheduleService.calculateAchievementRate(userId, date)));

            date = date.plusDays(1);
        }

        reportDailyStatService.replaceDailyStats(report, dailyStats);

        return report;
    }
}
