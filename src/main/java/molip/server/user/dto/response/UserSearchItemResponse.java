package molip.server.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "회원 검색 결과 항목")
public record UserSearchItemResponse(
        @Schema(description = "사용자 ID", example = "2") Long userId,
        @Schema(description = "닉네임", example = "nick01") String nickname,
        @Schema(description = "이메일", example = "email@email.com") String email,
        @Schema(description = "프로필 이미지") ImageInfoResponse profileImage) {

    public static UserSearchItemResponse of(
            Long userId, String nickname, String email, ImageInfoResponse profileImage) {
        return new UserSearchItemResponse(userId, nickname, email, profileImage);
    }
}
