package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record VideoOnlineParticipantItemResponse(
        Long participantId,
        Long userId,
        String sessionId,
        Boolean cameraEnabled,
        OffsetDateTime onlineAt,
        OffsetDateTime lastHeartbeatAt) {

    public static VideoOnlineParticipantItemResponse of(
            Long participantId,
            Long userId,
            String sessionId,
            Boolean cameraEnabled,
            OffsetDateTime onlineAt,
            OffsetDateTime lastHeartbeatAt) {
        return new VideoOnlineParticipantItemResponse(
                participantId, userId, sessionId, cameraEnabled, onlineAt, lastHeartbeatAt);
    }
}
