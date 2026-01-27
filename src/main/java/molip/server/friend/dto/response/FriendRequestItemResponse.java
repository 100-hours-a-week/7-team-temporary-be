package molip.server.friend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "친구 요청 항목")
public record FriendRequestItemResponse(
        @Schema(description = "요청 ID", example = "123") Long requestId,
        @Schema(description = "보낸 사용자 ID", example = "5") Long fromUserId,
        @Schema(description = "보낸 사용자 이메일", example = "email@email.com") String fromUserEmail,
        @Schema(description = "보낸 사용자 닉네임", example = "nick05") String fromUserNickname,
        @Schema(description = "프로필 이미지") ImageInfoResponse profileImage,
        @Schema(description = "요청 생성 시각", example = "2026-01-13T10:10:10+09:00")
                OffsetDateTime createdAt) {}
