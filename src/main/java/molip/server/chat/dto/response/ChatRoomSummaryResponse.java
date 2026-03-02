package molip.server.chat.dto.response;

import java.util.List;

public record ChatRoomSummaryResponse(List<ChatRoomSummaryItemResponse> rooms) {

    public static ChatRoomSummaryResponse from(List<ChatRoomSummaryItemResponse> rooms) {
        return new ChatRoomSummaryResponse(rooms);
    }
}
