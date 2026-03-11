package molip.server.chat.dto.response;

import java.time.Instant;
import java.time.OffsetDateTime;

public record VideoTokenIssuedResponse(
        Long roomId, Long participantId, Instant expiresAt, OffsetDateTime at) {

    public static VideoTokenIssuedResponse of(
            Long roomId, Long participantId, Instant expiresAt, OffsetDateTime at) {
        return new VideoTokenIssuedResponse(roomId, participantId, expiresAt, at);
    }
}
