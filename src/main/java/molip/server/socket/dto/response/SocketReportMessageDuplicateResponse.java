package molip.server.socket.dto.response;

public record SocketReportMessageDuplicateResponse(
        Long reportId, Long inputMessageId, Long streamMessageId, String status) {

    public static SocketReportMessageDuplicateResponse of(
            Long reportId, Long inputMessageId, Long streamMessageId, String status) {
        return new SocketReportMessageDuplicateResponse(
                reportId, inputMessageId, streamMessageId, status);
    }
}
