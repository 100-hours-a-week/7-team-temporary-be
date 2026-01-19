package molip.server.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 수정 요청")
public record UpdateProfileImageRequest(
    @Schema(description = "이미지 키", example = "550e8400-e29b-41d4-a716-446655440000")
        String imageKey) {}
