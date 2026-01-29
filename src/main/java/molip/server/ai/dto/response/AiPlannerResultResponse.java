package molip.server.ai.dto.response;

import java.util.List;

public record AiPlannerResultResponse(
        Long userId,
        Long taskId,
        Long dayPlanId,
        String title,
        String type,
        String assignedBy,
        String assignmentStatus,
        String startAt,
        String endAt,
        List<AiPlannerChildResponse> children) {}
