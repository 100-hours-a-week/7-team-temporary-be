package molip.server.ai.dto.request;

import java.util.List;

public record AiPlannerRequest(
        AiPlannerUserRequest user, String startArrange, List<AiPlannerTaskRequest> schedules) {}
