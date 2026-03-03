package molip.server.socket.session;

import java.time.OffsetDateTime;

public record SocketSessionContext(
        String accessToken, String deviceId, Long userId, OffsetDateTime connectedAt) {

    public static SocketSessionContext of(
            String accessToken, String deviceId, Long userId, OffsetDateTime connectedAt) {
        return new SocketSessionContext(accessToken, deviceId, userId, connectedAt);
    }
}
