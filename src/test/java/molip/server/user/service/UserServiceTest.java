package molip.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.LocalTime;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import molip.server.user.event.UserProfileImageLinkedEvent;
import molip.server.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private UserService userService;

  @Test
  void 성공적으로_유저가_등록될경우_이벤트가_발행된다() {
    // given
    String email = "email@test.com";
    String password = "Password1!";
    String nickname = "nick";
    Gender gender = Gender.MALE;
    LocalDate birth = LocalDate.of(1990, 1, 1);
    FocusTimeZone focusTimeZone = FocusTimeZone.MORNING;
    LocalTime dayEndTime = LocalTime.of(22, 40);
    String imageKey = "550e8400-e29b-41d4-a716-446655440000";

    given(userRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(false);
    given(passwordEncoder.encode(password)).willReturn("encoded");

    Users savedUser =
        new Users(email, "encoded", nickname, gender, birth, focusTimeZone, dayEndTime);
    ReflectionTestUtils.setField(savedUser, "id", 1L);

    given(userRepository.save(any(Users.class))).willReturn(savedUser);

    // when
    userService.registerUser(
        email, password, nickname, gender, birth, focusTimeZone, dayEndTime, imageKey);

    // then
    then(eventPublisher).should(times(1)).publishEvent(any(UserProfileImageLinkedEvent.class));
  }

  @Test
  void 이메일이_중복되면_예외를_반환한다() {
    // given
    String email = "email@test.com";
    String password = "Password1!";
    String nickname = "nick";

    given(userRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(true);

    // when
    BaseException exception =
        assertThrows(
            BaseException.class,
            () ->
                userService.registerUser(
                    email,
                    password,
                    nickname,
                    Gender.MALE,
                    LocalDate.of(1990, 1, 1),
                    FocusTimeZone.MORNING,
                    LocalTime.of(22, 40),
                    null));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_CONFLICT);
  }

  @Test
  void 비밀번호가_20자를_초과하면_예외를_반환한다() {
    // given
    String email = "email@test.com";
    String password = "Password1!Password1!X";
    String nickname = "nick";

    // when
    BaseException exception =
        assertThrows(
            BaseException.class,
            () ->
                userService.registerUser(
                    email,
                    password,
                    nickname,
                    Gender.MALE,
                    LocalDate.of(1990, 1, 1),
                    FocusTimeZone.MORNING,
                    LocalTime.of(22, 40),
                    null));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_PASSWORD_TOO_LONG);
  }

  @Test
  void 비밀번호_정책을_위반하면_예외를_반환한다() {
    // given
    String email = "email@test.com";
    String password = "password1";
    String nickname = "nick";

    // when
    BaseException exception =
        assertThrows(
            BaseException.class,
            () ->
                userService.registerUser(
                    email,
                    password,
                    nickname,
                    Gender.MALE,
                    LocalDate.of(1990, 1, 1),
                    FocusTimeZone.MORNING,
                    LocalTime.of(22, 40),
                    null));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_PASSWORD_POLICY);
  }
}
