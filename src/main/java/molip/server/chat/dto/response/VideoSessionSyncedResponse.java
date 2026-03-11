package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record VideoSessionSyncedResponse(
        Long roomId, Long participantId, String sessionId, Boolean published, OffsetDateTime at) {

    public static VideoSessionSyncedResponse of(
            Long roomId,
            Long participantId,
            String sessionId,
            Boolean published,
            OffsetDateTime at) {
        return new VideoSessionSyncedResponse(roomId, participantId, sessionId, published, at);
    }
}
