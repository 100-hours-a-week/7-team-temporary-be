package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record ChatRoomSummaryItemResponse(
        Long roomId,
        int unreadCount,
        String lastMessagePreview,
        OffsetDateTime lastMessageSentAt,
        int participantsCount) {

    public static ChatRoomSummaryItemResponse of(
            Long roomId,
            int unreadCount,
            String lastMessagePreview,
            OffsetDateTime lastMessageSentAt,
            int participantsCount) {
        return new ChatRoomSummaryItemResponse(
                roomId, unreadCount, lastMessagePreview, lastMessageSentAt, participantsCount);
    }
}
