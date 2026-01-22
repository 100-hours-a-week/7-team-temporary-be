package molip.server.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 수정 요청")
public record UpdatePasswordRequest(
        @Schema(description = "새 비밀번호", example = "newPassword") String newPassword,
        @Schema(description = "새 비밀번호 확인", example = "newPassword") String checkNewPassword) {}
