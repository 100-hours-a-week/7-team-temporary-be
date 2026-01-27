package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import molip.server.common.enums.ChatRoomType;

@Schema(description = "채팅방 상세")
public record ChatRoomDetailResponse(
        @Schema(description = "채팅방 ID", example = "10") Long roomId,
        @Schema(description = "타입", example = "OPEN_CHAT") ChatRoomType type,
        @Schema(description = "제목", example = "삼전 적정가는 18만이다.") String title,
        @Schema(description = "설명", example = "삼전이 18만원이 적정가인가에 대한 토론방") String description,
        @Schema(description = "최대 인원", example = "10") int maxParticipants,
        @Schema(description = "방장 정보") ChatRoomOwnerResponse owner,
        @Schema(description = "참가자 목록") List<ChatRoomParticipantResponse> participants,
        @Schema(description = "참가자 수", example = "2") int participantsCount) {}
