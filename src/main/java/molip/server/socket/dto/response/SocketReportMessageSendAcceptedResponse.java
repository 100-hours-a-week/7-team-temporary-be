package molip.server.socket.dto.response;

public record SocketReportMessageSendAcceptedResponse(
        Long reportId, Long inputMessageId, Long streamMessageId, String status) {

    public static SocketReportMessageSendAcceptedResponse of(
            Long reportId, Long inputMessageId, Long streamMessageId, String status) {

        return new SocketReportMessageSendAcceptedResponse(
                reportId, inputMessageId, streamMessageId, status);
    }
}
