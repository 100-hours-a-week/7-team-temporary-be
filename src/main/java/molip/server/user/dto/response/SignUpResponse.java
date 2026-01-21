package molip.server.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignUpResponse(@Schema(description = "사용자 ID", example = "3") Long userId) {

  public static SignUpResponse from(Long userId) {
    return new SignUpResponse(userId);
  }
}
