package molip.server.socket.dto.response;

public record SocketVideoReconnectRequiredResponse(
        String code, String message, boolean retryable, Long retryAfterMs) {

    public static SocketVideoReconnectRequiredResponse of(
            String code, String message, boolean retryable, Long retryAfterMs) {
        return new SocketVideoReconnectRequiredResponse(code, message, retryable, retryAfterMs);
    }
}
