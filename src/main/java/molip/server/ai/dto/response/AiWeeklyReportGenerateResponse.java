package molip.server.ai.dto.response;

public record AiWeeklyReportGenerateResponse(
        boolean success, double processTime, int count, String message) {}
