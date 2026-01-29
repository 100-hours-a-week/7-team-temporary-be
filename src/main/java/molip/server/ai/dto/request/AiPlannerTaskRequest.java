package molip.server.ai.dto.request;

public record AiPlannerTaskRequest(
        Long taskId,
        Long dayPlanId,
        String title,
        String type,
        String startAt,
        String endAt,
        String estimatedTimeRange,
        Integer focusLevel,
        Boolean isUrgent) {}
