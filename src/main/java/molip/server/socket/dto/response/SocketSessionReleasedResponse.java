package molip.server.socket.dto.response;

public record SocketSessionReleasedResponse(Long userId, String sessionId, boolean canReconnect) {

    public static SocketSessionReleasedResponse of(
            Long userId, String sessionId, boolean canReconnect) {

        return new SocketSessionReleasedResponse(userId, sessionId, canReconnect);
    }
}
