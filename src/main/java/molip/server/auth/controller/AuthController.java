package molip.server.auth.controller;

import java.time.Duration;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AuthTokens;
import molip.server.auth.dto.response.TokenResponse;
import molip.server.auth.service.AuthService;
import molip.server.common.SuccessCode;
import molip.server.common.response.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {
  private final AuthService authService;
  private final long refreshTokenExpirationMs;

  public AuthController(
      AuthService authService,
      @Value("${jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {
    this.authService = authService;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
  }

  @PostMapping("/token")
  @Override
  public ResponseEntity<ServerResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
    AuthTokens tokens = authService.login(request);
    TokenResponse response = new TokenResponse(tokens.accessToken());
    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", tokens.refreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/token")
            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
            .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ServerResponse.success(SuccessCode.LOGIN_SUCCESS, response));
  }

  @DeleteMapping("/token")
  @Override
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/token")
  @Override
  public ResponseEntity<ServerResponse<TokenResponse>> refresh() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
