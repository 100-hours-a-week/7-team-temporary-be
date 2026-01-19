package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.ChatRoomType;

@Schema(description = "채팅방 검색 항목")
public record ChatRoomSearchItemResponse(
    @Schema(description = "채팅방 ID", example = "10") Long roomId,
    @Schema(description = "타입", example = "OPEN_CHAT") ChatRoomType type,
    @Schema(description = "제목", example = "삼전 적정가는 18만이다.") String title,
    @Schema(description = "설명", example = "자료 공유방") String description,
    @Schema(description = "최대 인원", example = "50") int maxParticipants,
    @Schema(description = "참여자 수", example = "12") int participantsCount) {}
