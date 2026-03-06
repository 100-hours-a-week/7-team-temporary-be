package molip.server.chat.dto.response;

public record ChatRoomOwnerCheckResponse(boolean isOwner) {

    public static ChatRoomOwnerCheckResponse from(boolean isOwner) {
        return new ChatRoomOwnerCheckResponse(isOwner);
    }
}
