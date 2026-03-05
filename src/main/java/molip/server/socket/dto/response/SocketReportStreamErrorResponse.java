package molip.server.socket.dto.response;

public record SocketReportStreamErrorResponse(
        Long reportId, Long messageId, String status, String errorCode, String message) {

    public static SocketReportStreamErrorResponse of(
            Long reportId, Long messageId, String status, String errorCode, String message) {
        return new SocketReportStreamErrorResponse(reportId, messageId, status, errorCode, message);
    }
}
