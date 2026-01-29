package molip.server.ai.dto.response;

import java.util.List;

public record AiPlannerResponse(
        boolean success, Double processTime, List<AiPlannerResultResponse> results) {}
