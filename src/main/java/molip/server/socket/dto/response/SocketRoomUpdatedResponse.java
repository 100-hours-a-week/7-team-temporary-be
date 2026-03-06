package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketRoomUpdatedResponse(
        String eventId,
        Long roomId,
        String title,
        String description,
        Integer maxParticipants,
        OffsetDateTime updatedAt) {

    public static SocketRoomUpdatedResponse of(
            String eventId,
            Long roomId,
            String title,
            String description,
            Integer maxParticipants,
            OffsetDateTime updatedAt) {
        return new SocketRoomUpdatedResponse(
                eventId, roomId, title, description, maxParticipants, updatedAt);
    }
}
