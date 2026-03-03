package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketRoomDeletedResponse(String eventId, Long roomId, OffsetDateTime deletedAt) {

    public static SocketRoomDeletedResponse of(
            String eventId, Long roomId, OffsetDateTime deletedAt) {
        return new SocketRoomDeletedResponse(eventId, roomId, deletedAt);
    }
}
