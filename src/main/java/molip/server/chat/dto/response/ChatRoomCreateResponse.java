package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 생성 응답")
public record ChatRoomCreateResponse(
        @Schema(description = "채팅방 ID", example = "101") Long roomId,
        @Schema(description = "참여자 ID", example = "100") Long participantId) {

    public static ChatRoomCreateResponse from(Long roomId, Long participantId) {
        return new ChatRoomCreateResponse(roomId, participantId);
    }
}
