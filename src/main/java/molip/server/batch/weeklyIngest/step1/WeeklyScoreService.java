package molip.server.batch.weeklyIngest.step1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import molip.server.common.enums.ScheduleStatus;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportDailyStat;
import molip.server.report.repository.ReportDailyStatRepository;
import molip.server.report.repository.ReportRepository;
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.schedule.repository.ScheduleStatusCount;
import molip.server.user.entity.Users;

public final class WeeklyScoreService {

    private WeeklyScoreService() {}

    public static void processUser(
            Users user,
            LocalDate periodStart,
            LocalDate periodEnd,
            ScheduleRepository scheduleRepository,
            ReportRepository reportRepository,
            ReportDailyStatRepository reportDailyStatRepository) {
        Report report =
                reportRepository
                        .findByUserIdAndStartDateAndEndDate(user.getId(), periodStart, periodEnd)
                        .orElseGet(
                                () ->
                                        reportRepository.save(
                                                new Report(user, periodStart, periodEnd, 2, 0)));

        reportDailyStatRepository.deleteByReportId(report.getId());

        List<ReportDailyStat> stats = new ArrayList<>();

        LocalDate date = periodStart;
        for (int i = 0; i < 7; i++) {
            int achievementRate = calculateAchievementRate(user.getId(), date, scheduleRepository);
            stats.add(new ReportDailyStat(report, date, achievementRate));
            date = date.plusDays(1);
        }

        if (!stats.isEmpty()) {
            reportDailyStatRepository.saveAll(stats);
        }
    }

    private static int calculateAchievementRate(
            Long userId, LocalDate planDate, ScheduleRepository scheduleRepository) {
        List<ScheduleStatusCount> rows =
                scheduleRepository.countAssignedByStatus(
                        userId, planDate, List.of(ScheduleStatus.DONE, ScheduleStatus.TODO));

        Map<ScheduleStatus, Long> counts = new EnumMap<>(ScheduleStatus.class);
        for (ScheduleStatusCount row : rows) {
            counts.put(row.getStatus(), row.getCount());
        }

        long done = counts.getOrDefault(ScheduleStatus.DONE, 0L);
        long total = done + counts.getOrDefault(ScheduleStatus.TODO, 0L);
        if (total == 0) {
            return 0;
        }
        return (int) Math.round((done * 100.0) / total);
    }
}
