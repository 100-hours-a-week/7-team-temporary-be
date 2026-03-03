package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketMessageUpdatedResponse(
        String eventId, Long roomId, Long messageId, String content, OffsetDateTime updatedAt) {

    public static SocketMessageUpdatedResponse of(
            String eventId, Long roomId, Long messageId, String content, OffsetDateTime updatedAt) {

        return new SocketMessageUpdatedResponse(eventId, roomId, messageId, content, updatedAt);
    }
}
