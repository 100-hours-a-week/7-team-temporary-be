package molip.server.chat.redis.presence;

import java.time.OffsetDateTime;

public record ChatParticipantPresenceState(
        Long roomId,
        Long participantId,
        Long userId,
        String sessionId,
        OffsetDateTime onlineAt,
        OffsetDateTime lastHeartbeatAt) {

    public static ChatParticipantPresenceState of(
            Long roomId,
            Long participantId,
            Long userId,
            String sessionId,
            OffsetDateTime onlineAt,
            OffsetDateTime lastHeartbeatAt) {
        return new ChatParticipantPresenceState(
                roomId, participantId, userId, sessionId, onlineAt, lastHeartbeatAt);
    }

    public ChatParticipantPresenceState withHeartbeatAt(OffsetDateTime heartbeatAt) {
        return new ChatParticipantPresenceState(
                roomId, participantId, userId, sessionId, onlineAt, heartbeatAt);
    }
}
