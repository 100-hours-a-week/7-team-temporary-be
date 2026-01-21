package molip.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AuthResponse;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.store.DeviceStore;
import molip.server.auth.store.RefreshTokenStore;
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
}
