package molip.server.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AccessTokenResponse;
import molip.server.auth.dto.response.AuthResponse;
import molip.server.auth.service.AuthService;
import molip.server.common.SuccessCode;
import molip.server.common.response.ServerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
  public ResponseEntity<ServerResponse<AccessTokenResponse>> login(
      @RequestBody LoginRequest request,
      @CookieValue(name = "deviceId", required = false) String deviceId) {
    AuthResponse tokens = authService.login(request, deviceId);
    AccessTokenResponse response = new AccessTokenResponse(tokens.accessToken());
    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", tokens.refreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/token")
            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
            .build();
    ResponseCookie deviceCookie =
        ResponseCookie.from("deviceId", tokens.deviceId())
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/token")
            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
            .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
        .body(ServerResponse.success(SuccessCode.LOGIN_SUCCESS, response));
  }

  @DeleteMapping("/token")
  @Override
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String accessToken = resolveToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    authService.logout(accessToken);

    return ResponseEntity.noContent().build();
  }

  @PutMapping("/token")
  @Override
  public ResponseEntity<ServerResponse<AccessTokenResponse>> refresh(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    AuthResponse tokens = authService.reissue(refreshToken);
    AccessTokenResponse response = new AccessTokenResponse(tokens.accessToken());
    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", tokens.refreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/token")
            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
            .build();
    ResponseCookie deviceCookie =
        ResponseCookie.from("deviceId", tokens.deviceId())
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/token")
            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
            .build();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    headers.add(HttpHeaders.SET_COOKIE, deviceCookie.toString());

    return ResponseEntity.ok()
        .headers(headers)
        .body(ServerResponse.success(SuccessCode.TOKEN_REISSUE_SUCCESS, response));
  }

  private String resolveToken(String authorization) {
    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }
    return null;
  }
}
