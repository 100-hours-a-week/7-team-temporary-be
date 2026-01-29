package molip.server.ai.dto.request;

public record AiPlannerUserRequest(Long userId, String focusTimeZone, String dayEndTime) {}
