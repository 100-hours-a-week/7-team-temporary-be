package molip.server.socket.dto.response;

public record SocketMessageSendRejectedResponse(
        String idempotencyKey, String code, String message, boolean retryable) {

    public static SocketMessageSendRejectedResponse of(
            String idempotencyKey, String code, String message, boolean retryable) {
        return new SocketMessageSendRejectedResponse(idempotencyKey, code, message, retryable);
    }
}
