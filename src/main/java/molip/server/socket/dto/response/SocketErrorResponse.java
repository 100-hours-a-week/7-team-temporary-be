package molip.server.socket.dto.response;

public record SocketErrorResponse(String code, String message, boolean retryable) {

    public static SocketErrorResponse of(String code, String message, boolean retryable) {
        return new SocketErrorResponse(code, message, retryable);
    }
}
