package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record VideoRoomDeletedResponse(
        String eventId, Long roomId, OffsetDateTime deletedAt, String reason) {

    public static VideoRoomDeletedResponse of(
            String eventId, Long roomId, OffsetDateTime deletedAt, String reason) {
        return new VideoRoomDeletedResponse(eventId, roomId, deletedAt, reason);
    }
}
