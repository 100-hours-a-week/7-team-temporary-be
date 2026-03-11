package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record VideoPublishChangedResponse(
        String eventId,
        Long roomId,
        Long participantId,
        Long userId,
        String sessionId,
        OffsetDateTime at) {

    public static VideoPublishChangedResponse of(
            String eventId,
            Long roomId,
            Long participantId,
            Long userId,
            String sessionId,
            OffsetDateTime at) {
        return new VideoPublishChangedResponse(
                eventId, roomId, participantId, userId, sessionId, at);
    }
}
