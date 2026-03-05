package molip.server.socket.dto.response;

public record SocketReportMessageCancelRejectedResponse(
        Long reportId, Long messageId, String code, String message, boolean retryable) {

    public static SocketReportMessageCancelRejectedResponse of(
            Long reportId, Long messageId, String code, String message, boolean retryable) {
        return new SocketReportMessageCancelRejectedResponse(
                reportId, messageId, code, message, retryable);
    }
}
