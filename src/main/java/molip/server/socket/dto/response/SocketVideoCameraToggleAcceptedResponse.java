package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketVideoCameraToggleAcceptedResponse(
        String requestId,
        Long roomId,
        Long participantId,
        Boolean cameraEnabled,
        String status,
        OffsetDateTime at) {

    public static SocketVideoCameraToggleAcceptedResponse of(
            String requestId,
            Long roomId,
            Long participantId,
            Boolean cameraEnabled,
            String status,
            OffsetDateTime at) {
        return new SocketVideoCameraToggleAcceptedResponse(
                requestId, roomId, participantId, cameraEnabled, status, at);
    }
}
