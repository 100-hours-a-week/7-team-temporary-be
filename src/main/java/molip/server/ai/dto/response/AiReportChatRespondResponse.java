package molip.server.ai.dto.response;

public record AiReportChatRespondResponse(
        boolean success, Double processTime, AiReportChatRespondData data) {}
