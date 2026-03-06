package molip.server.socket.dto.response;

public record SocketReportMessageSendRejectedResponse(
        Long reportId, String code, String message, boolean retryable) {

    public static SocketReportMessageSendRejectedResponse of(
            Long reportId, String code, String message, boolean retryable) {
        return new SocketReportMessageSendRejectedResponse(reportId, code, message, retryable);
    }
}
