package molip.server.ai.dto.response;

import java.util.List;

public record AiWeeklyReportFetchResponse(boolean success, List<WeeklyReportFetchResult> results) {

    public record WeeklyReportFetchResult(
            Long reportId, Long userId, String status, String content) {}
}
