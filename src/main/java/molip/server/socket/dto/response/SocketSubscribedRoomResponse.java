package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketSubscribedRoomResponse(
        Long roomId, Long participantId, OffsetDateTime subscribedAt) {

    public static SocketSubscribedRoomResponse of(
            Long roomId, Long participantId, OffsetDateTime subscribedAt) {
        return new SocketSubscribedRoomResponse(roomId, participantId, subscribedAt);
    }
}
