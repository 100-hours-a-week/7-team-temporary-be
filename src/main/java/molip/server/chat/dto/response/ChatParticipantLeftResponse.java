package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record ChatParticipantLeftResponse(
        String eventId, Long roomId, Long participantId, Long userId, OffsetDateTime leftAt) {

    public static ChatParticipantLeftResponse of(
            String eventId, Long roomId, Long participantId, Long userId, OffsetDateTime leftAt) {
        return new ChatParticipantLeftResponse(eventId, roomId, participantId, userId, leftAt);
    }
}
