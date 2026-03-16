package molip.server.chat.redis.presence;

import java.time.OffsetDateTime;

public record VideoParticipantPresenceState(
        Long roomId,
        Long participantId,
        Long userId,
        String sessionId,
        Boolean cameraEnabled,
        OffsetDateTime onlineAt,
        OffsetDateTime lastHeartbeatAt) {

    public static VideoParticipantPresenceState of(
            Long roomId,
            Long participantId,
            Long userId,
            String sessionId,
            Boolean cameraEnabled,
            OffsetDateTime onlineAt,
            OffsetDateTime lastHeartbeatAt) {
        return new VideoParticipantPresenceState(
                roomId, participantId, userId, sessionId, cameraEnabled, onlineAt, lastHeartbeatAt);
    }

    public VideoParticipantPresenceState withHeartbeatAt(OffsetDateTime heartbeatAt) {
        return new VideoParticipantPresenceState(
                roomId, participantId, userId, sessionId, cameraEnabled, onlineAt, heartbeatAt);
    }
}
