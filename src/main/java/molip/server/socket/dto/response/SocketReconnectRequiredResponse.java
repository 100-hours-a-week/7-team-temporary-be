package molip.server.socket.dto.response;

public record SocketReconnectRequiredResponse(String code, String message, Long retryAfterMs) {

    public static SocketReconnectRequiredResponse of(
            String code, String message, Long retryAfterMs) {
        return new SocketReconnectRequiredResponse(code, message, retryAfterMs);
    }
}
