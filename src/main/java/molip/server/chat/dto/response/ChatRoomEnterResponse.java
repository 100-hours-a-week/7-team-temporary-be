package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 입장 응답")
public record ChatRoomEnterResponse(
        @Schema(description = "참가자 ID", example = "101") Long participantId) {}
