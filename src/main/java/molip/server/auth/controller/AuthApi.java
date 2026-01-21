package molip.server.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AccessTokenResponse;
import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;

@Tag(name = "Auth", description = "인증/인가 API")
public interface AuthApi {

  @Operation(summary = "로그인", description = "refreshToken, deviceId는 쿠키로 전달됩니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "필수 값 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "아이디/비밀번호 불일치",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<AccessTokenResponse>> login(
      LoginRequest request, @CookieValue(name = "deviceId", required = false) String deviceId);

  @Operation(summary = "로그아웃", description = "전 디바이스 로그아웃")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<Void> logout(HttpServletRequest request);

  @Operation(summary = "토큰 재발급", description = "refreshToken은 쿠키로 전달됩니다.")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "재발급 성공",
        content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "리프레시 토큰 누락",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  ResponseEntity<ServerResponse<AccessTokenResponse>> refresh(
      @CookieValue(name = "refreshToken", required = false) String refreshToken);
}
