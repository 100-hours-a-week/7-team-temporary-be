package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketVideoErrorResponse(
        Long roomId,
        Long participantId,
        String code,
        String message,
        boolean retryable,
        OffsetDateTime at) {

    public static SocketVideoErrorResponse of(
            Long roomId,
            Long participantId,
            String code,
            String message,
            boolean retryable,
            OffsetDateTime at) {
        return new SocketVideoErrorResponse(roomId, participantId, code, message, retryable, at);
    }
}
