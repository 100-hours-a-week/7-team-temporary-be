package molip.server.socket.dto.response;

public record SocketErrorResponse(String code, String message) {

    public static SocketErrorResponse of(String code, String message) {
        return new SocketErrorResponse(code, message);
    }
}
