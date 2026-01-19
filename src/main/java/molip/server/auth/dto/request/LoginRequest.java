package molip.server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequest(
    @Schema(description = "이메일", example = "email@email.com") String email,
    @Schema(description = "비밀번호", example = "password") String password) {}
