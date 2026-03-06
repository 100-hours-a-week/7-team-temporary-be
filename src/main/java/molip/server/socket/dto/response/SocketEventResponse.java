package molip.server.socket.dto.response;

public record SocketEventResponse<T>(String event, T payload) {

    public static <T> SocketEventResponse<T> of(String event, T payload) {
        return new SocketEventResponse<>(event, payload);
    }
}
