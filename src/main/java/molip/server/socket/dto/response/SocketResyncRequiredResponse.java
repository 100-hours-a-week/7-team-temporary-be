package molip.server.socket.dto.response;

public record SocketResyncRequiredResponse(String scope, Long roomId, Long fromMessageId) {

    public static SocketResyncRequiredResponse of(String scope, Long roomId, Long fromMessageId) {
        return new SocketResyncRequiredResponse(scope, roomId, fromMessageId);
    }
}
