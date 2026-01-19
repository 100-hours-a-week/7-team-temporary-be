package molip.server.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 참가자 상태 변경 요청")
public record ChatRoomParticipantUpdateRequest(
    @Schema(description = "카메라 활성화 여부", example = "true") Boolean cameraEnabled,
    @Schema(description = "마지막 확인 메시지 ID", example = "165") Long lastSeenMessageId) {}
