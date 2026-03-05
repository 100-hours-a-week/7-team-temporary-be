package molip.server.ai.dto.request;

import java.util.List;

public record AiWeeklyReportFetchRequest(List<WeeklyReportFetchTarget> targets) {

    public static AiWeeklyReportFetchRequest of(List<WeeklyReportFetchTarget> targets) {
        return new AiWeeklyReportFetchRequest(targets);
    }

    public record WeeklyReportFetchTarget(Long reportId, Long userId) {

        public static WeeklyReportFetchTarget of(Long reportId, Long userId) {
            return new WeeklyReportFetchTarget(reportId, userId);
        }
    }
}
