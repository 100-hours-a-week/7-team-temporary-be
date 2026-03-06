package molip.server.chat.dto.response;

public record ChatRoomUnreadCountResponse(Long roomId, int unreadCount) {

    public static ChatRoomUnreadCountResponse of(Long roomId, int unreadCount) {
        return new ChatRoomUnreadCountResponse(roomId, unreadCount);
    }
}
