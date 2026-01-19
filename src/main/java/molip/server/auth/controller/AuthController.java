package molip.server.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.TokenResponse;
import molip.server.common.response.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/token")
public class AuthController {
  @Operation(summary = "로그인", description = "refreshToken은 쿠키로 전달됩니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = TokenResponse.class))),
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
  @PostMapping
  public ResponseEntity<ServerResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }

  @Operation(summary = "로그아웃", description = "전 디바이스 로그아웃")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "로그아웃 성공"),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(schema = @Schema(implementation = ServerResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ServerResponse.class)))
  })
  @DeleteMapping
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "토큰 재발급", description = "refreshToken은 쿠키로 전달됩니다.")
  @SecurityRequirement(name = "JWT")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "재발급 성공",
        content = @Content(schema = @Schema(implementation = TokenResponse.class))),
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
  @PutMapping
  public ResponseEntity<ServerResponse<TokenResponse>> refresh() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
