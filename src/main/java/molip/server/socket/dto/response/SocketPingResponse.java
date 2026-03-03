package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketPingResponse(OffsetDateTime timestamp) {

    public static SocketPingResponse of(OffsetDateTime timestamp) {
        return new SocketPingResponse(timestamp);
    }
}
