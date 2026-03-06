package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record ChatRoomSummaryItemResponse(
        Long roomId,
        int unreadCount,
        String lastUserMessagePreview,
        OffsetDateTime lastUserMessageSentAt,
        int participantsCount) {

    public static ChatRoomSummaryItemResponse of(
            Long roomId,
            int unreadCount,
            String lastUserMessagePreview,
            OffsetDateTime lastUserMessageSentAt,
            int participantsCount) {
        return new ChatRoomSummaryItemResponse(
                roomId,
                unreadCount,
                lastUserMessagePreview,
                lastUserMessageSentAt,
                participantsCount);
    }
}
