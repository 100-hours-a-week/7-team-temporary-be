package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record VideoParticipantPresenceResponse(
        String eventId,
        Long roomId,
        Long participantId,
        Long userId,
        String nickname,
        String sessionId,
        Boolean cameraEnabled,
        OffsetDateTime at) {

    public static VideoParticipantPresenceResponse of(
            String eventId,
            Long roomId,
            Long participantId,
            Long userId,
            String nickname,
            String sessionId,
            Boolean cameraEnabled,
            OffsetDateTime at) {
        return new VideoParticipantPresenceResponse(
                eventId, roomId, participantId, userId, nickname, sessionId, cameraEnabled, at);
    }
}
