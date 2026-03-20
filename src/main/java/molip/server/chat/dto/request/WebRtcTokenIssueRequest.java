package molip.server.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "WebRTC 토큰 발급 요청")
public record WebRtcTokenIssueRequest(
        @NotNull @Schema(description = "채팅방 참가자 ID", example = "101") Long participantId) {}
