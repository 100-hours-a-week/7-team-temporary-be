package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개인 채팅방 입장 응답")
public record ChatDirectRoomEnterResponse(
        @Schema(description = "채팅방 ID", example = "200") Long roomId,
        @Schema(description = "동작", example = "JOINED") String action) {

    public static ChatDirectRoomEnterResponse of(Long roomId, String action) {
        return new ChatDirectRoomEnterResponse(roomId, action);
    }
}
