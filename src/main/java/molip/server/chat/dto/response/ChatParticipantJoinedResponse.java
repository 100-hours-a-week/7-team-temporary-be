package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record ChatParticipantJoinedResponse(
        String eventId,
        Long roomId,
        Long participantId,
        Long userId,
        String nickname,
        Boolean cameraEnabled,
        OffsetDateTime joinedAt) {

    public static ChatParticipantJoinedResponse of(
            String eventId,
            Long roomId,
            Long participantId,
            Long userId,
            String nickname,
            Boolean cameraEnabled,
            OffsetDateTime joinedAt) {
        return new ChatParticipantJoinedResponse(
                eventId, roomId, participantId, userId, nickname, cameraEnabled, joinedAt);
    }
}
