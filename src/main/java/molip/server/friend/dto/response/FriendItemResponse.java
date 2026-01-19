package molip.server.friend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "친구 목록 항목")
public record FriendItemResponse(
    @Schema(description = "친구 사용자 ID", example = "5") Long friendUserId,
    @Schema(description = "친구 이메일", example = "email@email.com") String friendEmail,
    @Schema(description = "친구 닉네임", example = "nick05") String friendNickname,
    @Schema(description = "프로필 이미지") ImageInfoResponse profileImage) {}
