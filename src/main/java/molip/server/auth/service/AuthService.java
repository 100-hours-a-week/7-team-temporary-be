package molip.server.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AuthResponse;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.store.DeviceStore;
import molip.server.auth.store.RefreshTokenStore;
import molip.server.auth.store.TokenBlacklistStore;
import molip.server.auth.store.TokenVersionStore;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,20}$");

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final TokenBlacklistStore tokenBlacklistStore;
  private final TokenVersionStore tokenVersionStore;
  private final RefreshTokenStore refreshTokenStore;
  private final DeviceStore deviceStore;

  public AuthResponse login(LoginRequest request, String deviceId) {
    validateEmail(request.email());
    validatePassword(request.password());

    Users user =
        userRepository
            .findByEmailAndDeletedAtIsNull(request.email())
            .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BaseException(ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS);
    }

    String resolvedDeviceId = deviceId;
    if (resolvedDeviceId == null || resolvedDeviceId.isBlank()) {
      resolvedDeviceId = UUID.randomUUID().toString();
    }
    String accessJti = UUID.randomUUID().toString();
    String refreshJti = UUID.randomUUID().toString();
    long tokenVersion = tokenVersionStore.getOrInit(user.getId());

    String accessToken =
        jwtUtil.createAccessToken(user.getId(), "USER", tokenVersion, resolvedDeviceId, accessJti);
    String refreshToken =
        jwtUtil.createRefreshToken(
            user.getId(), "USER", tokenVersion, resolvedDeviceId, refreshJti);

    refreshTokenStore.save(user.getId(), resolvedDeviceId, hashRefreshToken(refreshToken));
    deviceStore.addDevice(user.getId(), resolvedDeviceId);
    log.info(
        "Login tokens stored. userId={}, deviceId={}, tokenVersion={}",
        user.getId(),
        resolvedDeviceId,
        tokenVersion);

    return new AuthResponse(accessToken, refreshToken, resolvedDeviceId);
  }

  public void logout(String accessToken) {
    Long userId = jwtUtil.extractUserId(accessToken);
    if (userId == null) {
      throw new BaseException(ErrorCode.UNAUTHORIZED_INVALID_TOKEN);
    }

    tokenBlacklistStore.add(userId, accessToken);
    tokenVersionStore.increment(userId);

    var deviceIds = deviceStore.listDevices(userId);
    refreshTokenStore.deleteAll(userId, deviceIds);
    deviceStore.clearDevices(userId);

    log.info("Logout processed. userId={}", userId);
  }

  private void validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
    }
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST_EMAIL_POLICY);
    }
  }

  private void validatePassword(String password) {
    if (password == null || password.isBlank()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
    }
    if (password.length() > 20) {
      throw new BaseException(ErrorCode.INVALID_REQUEST_PASSWORD_TOO_LONG);
    }
    if (!PASSWORD_PATTERN.matcher(password).matches()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST_PASSWORD_POLICY);
    }
  }

  private String hashRefreshToken(String refreshToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte value : hashed) {
        builder.append(String.format("%02x", value));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
