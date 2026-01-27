package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "채팅방 방장 정보")
public record ChatRoomOwnerResponse(
        @Schema(description = "사용자 ID", example = "1") Long userId,
        @Schema(description = "닉네임", example = "ownerNick") String nickname,
        @Schema(description = "카메라 활성화 여부", example = "false") boolean cameraEnabled,
        @Schema(description = "프로필 이미지") ImageInfoResponse profileImage) {}
