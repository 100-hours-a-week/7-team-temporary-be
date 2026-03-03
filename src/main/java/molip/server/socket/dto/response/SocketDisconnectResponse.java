package molip.server.socket.dto.response;

public record SocketDisconnectResponse(String code, String message) {

    public static SocketDisconnectResponse of(String code, String message) {
        return new SocketDisconnectResponse(code, message);
    }
}
