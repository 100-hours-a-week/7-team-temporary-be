package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "채팅방 참가자")
public record ChatRoomParticipantResponse(
    @Schema(description = "참가자 ID", example = "101") Long participantId,
    @Schema(description = "사용자 ID", example = "5") Long userId,
    @Schema(description = "닉네임", example = "nick05") String nickname,
    @Schema(description = "카메라 활성화 여부", example = "false") boolean cameraEnabled,
    @Schema(description = "프로필 이미지") ImageInfoResponse profileImage,
    @Schema(description = "참가 시각", example = "2026-01-13T10:10:10+09:00")
        OffsetDateTime joinedAt) {}
