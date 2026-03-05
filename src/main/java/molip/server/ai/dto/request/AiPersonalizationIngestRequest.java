package molip.server.ai.dto.request;

import java.time.LocalDate;
import java.util.List;

public record AiPersonalizationIngestRequest(List<Long> userIds, String targetDate) {

    public static AiPersonalizationIngestRequest of(List<Long> userIds, LocalDate targetDate) {
        return new AiPersonalizationIngestRequest(userIds, targetDate.toString());
    }
}
