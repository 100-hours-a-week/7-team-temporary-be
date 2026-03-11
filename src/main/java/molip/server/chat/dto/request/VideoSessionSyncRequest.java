package molip.server.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비디오 세션 동기화 요청")
public record VideoSessionSyncRequest(
        @Schema(description = "채팅방 참가자 ID", example = "101") Long participantId,
        @Schema(description = "LiveKit session ID", example = "lk-sid-123") String sessionId,
        @Schema(description = "현재 publish 여부", example = "true") Boolean published) {}
