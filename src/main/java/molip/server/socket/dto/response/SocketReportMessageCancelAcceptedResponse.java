package molip.server.socket.dto.response;

public record SocketReportMessageCancelAcceptedResponse(
        Long reportId, Long messageId, String status) {

    public static SocketReportMessageCancelAcceptedResponse of(
            Long reportId, Long messageId, String status) {
        return new SocketReportMessageCancelAcceptedResponse(reportId, messageId, status);
    }
}
