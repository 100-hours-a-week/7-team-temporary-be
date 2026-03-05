package molip.server.ai.dto.request;

import java.time.LocalDate;
import java.util.List;

public record AiWeeklyReportGenerateRequest(LocalDate baseDate, List<UserReportTarget> users) {

    public static AiWeeklyReportGenerateRequest of(
            LocalDate baseDate, List<UserReportTarget> users) {
        return new AiWeeklyReportGenerateRequest(baseDate, users);
    }

    public record UserReportTarget(Long userId, Long reportId) {

        public static UserReportTarget of(Long userId, Long reportId) {
            return new UserReportTarget(userId, reportId);
        }
    }
}
