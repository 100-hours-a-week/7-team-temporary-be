package molip.server.socket.dto.response;

public record SocketVideoCameraToggleRejectedResponse(
        Long roomId,
        Long participantId,
        String requestId,
        String code,
        String message,
        boolean retryable) {

    public static SocketVideoCameraToggleRejectedResponse of(
            Long roomId,
            Long participantId,
            String requestId,
            String code,
            String message,
            boolean retryable) {
        return new SocketVideoCameraToggleRejectedResponse(
                roomId, participantId, requestId, code, message, retryable);
    }
}
