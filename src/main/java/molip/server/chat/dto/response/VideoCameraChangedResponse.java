package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record VideoCameraChangedResponse(
        String eventId,
        Long roomId,
        Long participantId,
        Long userId,
        Boolean cameraEnabled,
        OffsetDateTime at) {

    public static VideoCameraChangedResponse of(
            String eventId,
            Long roomId,
            Long participantId,
            Long userId,
            Boolean cameraEnabled,
            OffsetDateTime at) {
        return new VideoCameraChangedResponse(
                eventId, roomId, participantId, userId, cameraEnabled, at);
    }
}
