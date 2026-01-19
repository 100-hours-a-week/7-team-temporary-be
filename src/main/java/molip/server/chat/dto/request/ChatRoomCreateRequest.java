package molip.server.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 생성 요청")
public record ChatRoomCreateRequest(
    @Schema(description = "제목", example = "삼전 적정가는 18만이다.") String title,
    @Schema(description = "설명", example = "삼전이 18만원이 적정가인가에 대한 토론방") String description,
    @Schema(description = "최대 인원", example = "10") int maxParticipants) {}
