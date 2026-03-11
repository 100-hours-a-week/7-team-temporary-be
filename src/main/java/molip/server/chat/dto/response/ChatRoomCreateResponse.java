package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.ChatRoomType;

@Schema(description = "채팅방 생성 응답")
public record ChatRoomCreateResponse(
        @Schema(description = "채팅방 ID", example = "101") Long roomId,
        @Schema(description = "참여자 ID", example = "100") Long participantId,
        @Schema(description = "채팅방 타입", example = "OPEN_CHAT") ChatRoomType type) {

    public static ChatRoomCreateResponse from(Long roomId, Long participantId, ChatRoomType type) {
        return new ChatRoomCreateResponse(roomId, participantId, type);
    }
}
