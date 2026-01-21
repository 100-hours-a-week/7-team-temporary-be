package molip.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AuthResponse;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.store.DeviceStore;
import molip.server.auth.store.RefreshTokenStore;
import molip.server.auth.store.TokenBlacklistStore;
import molip.server.auth.store.TokenVersionStore;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;
  @Mock private TokenBlacklistStore tokenBlacklistStore;
  @Mock private TokenVersionStore tokenVersionStore;
  @Mock private RefreshTokenStore refreshTokenStore;
  @Mock private DeviceStore deviceStore;

  @InjectMocks private AuthService authService;

  @Test
  void 로그인에_성공하면_토큰을_반환한다() {
    // given
    LoginRequest request = new LoginRequest("email@test.com", "Password1!");
    Users user =
        new Users(
            "email@test.com",
            "encoded",
            "nick",
            Gender.MALE,
            LocalDate.of(1990, 1, 1),
            FocusTimeZone.MORNING,
            LocalTime.of(22, 40));
    ReflectionTestUtils.setField(user, "id", 4L);

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
        .willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
    given(tokenVersionStore.getOrInit(user.getId())).willReturn(1L);
    given(jwtUtil.createAccessToken(any(), any(), any(), any(), any())).willReturn("access");
    given(jwtUtil.createRefreshToken(any(), any(), any(), any(), any())).willReturn("refresh");

    // when
    AuthResponse response = authService.login(request, null);

    // then
    assertThat(response.accessToken()).isEqualTo("access");
    assertThat(response.refreshToken()).isEqualTo("refresh");
  }

  @Test
  void 디바이스아이디가_있으면_해당_값을_사용한다() {
    // given
    LoginRequest request = new LoginRequest("email@test.com", "Password1!");
    Users user =
        new Users(
            "email@test.com",
            "encoded",
            "nick",
            Gender.MALE,
            LocalDate.of(1990, 1, 1),
            FocusTimeZone.MORNING,
            LocalTime.of(22, 40));
    ReflectionTestUtils.setField(user, "id", 4L);

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
        .willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
    given(tokenVersionStore.getOrInit(user.getId())).willReturn(1L);
    given(jwtUtil.createAccessToken(any(), any(), any(), any(), any())).willReturn("access");
    given(jwtUtil.createRefreshToken(any(), any(), any(), any(), any())).willReturn("refresh");

    // when
    AuthResponse response = authService.login(request, "device-123");

    // then
    assertThat(response.deviceId()).isEqualTo("device-123");
  }

  @Test
  void 비밀번호가_일치하지않으면_예외를_반환한다() {
    // given
    LoginRequest request = new LoginRequest("email@test.com", "Password1!");
    Users user =
        new Users(
            "email@test.com",
            "encoded",
            "nick",
            Gender.MALE,
            LocalDate.of(1990, 1, 1),
            FocusTimeZone.MORNING,
            LocalTime.of(22, 40));
    ReflectionTestUtils.setField(user, "id", 4L);

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
        .willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

    // when
    BaseException exception =
        assertThrows(BaseException.class, () -> authService.login(request, null));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_INVALID_CREDENTIALS);
  }

  @Test
  void 리프레시토큰_재발급에_성공하면_새_토큰을_반환한다() {
    // given
    given(jwtUtil.isExpired("refresh")).willReturn(false);
    given(jwtUtil.extractUserId("refresh")).willReturn(4L);
    given(jwtUtil.extractTokenVersion("refresh")).willReturn(1L);
    given(jwtUtil.extractDeviceId("refresh")).willReturn("device-1");
    given(tokenVersionStore.get(4L)).willReturn(1L);
    given(refreshTokenStore.matches(any(), any(), any())).willReturn(true);
    given(jwtUtil.createAccessToken(any(), any(), any(), any(), any())).willReturn("access-new");
    given(jwtUtil.createRefreshToken(any(), any(), any(), any(), any())).willReturn("refresh-new");

    // when
    AuthResponse response = authService.reissue("refresh");

    // then
    assertThat(response.accessToken()).isEqualTo("access-new");
    assertThat(response.refreshToken()).isEqualTo("refresh-new");
  }

  @Test
  void 리프레시토큰이_없으면_예외를_반환한다() {
    // given
    String refreshToken = "";

    // when
    BaseException exception =
        assertThrows(BaseException.class, () -> authService.reissue(refreshToken));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_REFRESH_MISSING);
  }

  @Test
  void 리프레시토큰이_만료되면_예외를_반환한다() {
    // given
    given(jwtUtil.isExpired("refresh")).willReturn(true);

    // when
    BaseException exception =
        assertThrows(BaseException.class, () -> authService.reissue("refresh"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_INVALID_TOKEN);
  }

  @Test
  void 리프레시토큰이_재사용되면_예외를_반환한다() {
    // given
    given(jwtUtil.isExpired("refresh")).willReturn(false);
    given(jwtUtil.extractUserId("refresh")).willReturn(4L);
    given(jwtUtil.extractTokenVersion("refresh")).willReturn(1L);
    given(jwtUtil.extractDeviceId("refresh")).willReturn("device-1");
    given(tokenVersionStore.get(4L)).willReturn(1L);
    given(refreshTokenStore.matches(any(), any(), any())).willReturn(false);
    given(deviceStore.listDevices(4L)).willReturn(java.util.Set.of("device-1"));

    // when
    BaseException exception =
        assertThrows(BaseException.class, () -> authService.reissue("refresh"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_INVALID_TOKEN);
  }

  @Test
  void 로그아웃하면_블랙리스트와_토큰버전을_갱신한다() {
    // given
    given(jwtUtil.extractUserId("access")).willReturn(4L);
    given(deviceStore.listDevices(4L)).willReturn(java.util.Set.of("device-1"));

    // when
    authService.logout("access");

    // then
    then(tokenBlacklistStore).should().add(4L, "access");
    then(tokenVersionStore).should().increment(4L);
  }

  @Test
  void 유효하지않은_토큰이면_로그아웃이_실패한다() {
    // given
    given(jwtUtil.extractUserId("access")).willReturn(null);

    // when
    BaseException exception = assertThrows(BaseException.class, () -> authService.logout("access"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_INVALID_TOKEN);
  }
}
