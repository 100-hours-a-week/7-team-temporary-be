package molip.server.socket.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소켓 마지막 확인 메시지 상태 변경 요청")
public record SocketLastSeenUpdateRequest(
        @Schema(description = "참가자 ID", example = "101") Long participantId,
        @Schema(description = "마지막 확인 메시지 ID", example = "165") Long lastSeenMessageId) {}
