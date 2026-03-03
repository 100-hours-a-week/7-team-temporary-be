package molip.server.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 수정 요청")
public record ChatMessageUpdateRequest(
        @Schema(description = "수정할 메시지 내용", example = "수정 메시지 내용입니다.") String content) {}
