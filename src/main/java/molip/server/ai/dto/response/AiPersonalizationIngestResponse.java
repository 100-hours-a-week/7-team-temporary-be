package molip.server.ai.dto.response;

import java.util.List;

public record AiPersonalizationIngestResponse(
        boolean success, Double processTime, List<Long> userIds, String message) {}
