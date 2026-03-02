package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketUnreadChangedResponse(
        String eventId,
        Long userId,
        Long roomId,
        int unreadCount,
        String lastMessagePreview,
        OffsetDateTime lastMessageSentAt,
        int participantsCount) {

    public static SocketUnreadChangedResponse of(
            String eventId,
            Long userId,
            Long roomId,
            int unreadCount,
            String lastMessagePreview,
            OffsetDateTime lastMessageSentAt,
            int participantsCount) {
        return new SocketUnreadChangedResponse(
                eventId,
                userId,
                roomId,
                unreadCount,
                lastMessagePreview,
                lastMessageSentAt,
                participantsCount);
    }
}
