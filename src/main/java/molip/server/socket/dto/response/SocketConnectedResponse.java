package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketConnectedResponse(
        String sessionId, Long userId, OffsetDateTime connectedAt, OffsetDateTime serverTime) {

    public static SocketConnectedResponse of(
            String sessionId, Long userId, OffsetDateTime connectedAt, OffsetDateTime serverTime) {

        return new SocketConnectedResponse(sessionId, userId, connectedAt, serverTime);
    }
}
