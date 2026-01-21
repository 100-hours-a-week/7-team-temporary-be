package molip.server.auth.controller;

import lombok.RequiredArgsConstructor;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AuthTokens;
import molip.server.auth.dto.response.TokenResponse;
import molip.server.auth.service.AuthService;
import molip.server.common.SuccessCode;
import molip.server.common.response.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
  private final AuthService authService;

  @PostMapping("/token")
  @Override
  public ResponseEntity<ServerResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
    AuthTokens tokens = authService.login(request);
    TokenResponse response = new TokenResponse(tokens.accessToken());
    return ResponseEntity.ok(ServerResponse.success(SuccessCode.LOGIN_SUCCESS, response));
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
