package molip.server.user.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,20}$");

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Users registerUser(
      String email,
      String password,
      String nickname,
      Gender gender,
      LocalDate birth,
      FocusTimeZone focusTimeZone,
      LocalTime dayEndTime) {
    validateEmail(email);
    validatePassword(password);
    validateDuplicatedUser(email, nickname);

    String encodedPassword = passwordEncoder.encode(password);

    return userRepository.save(
        new Users(email, encodedPassword, nickname, gender, birth, focusTimeZone, dayEndTime));
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
    if (!PASSWORD_PATTERN.matcher(password).matches()) {
      throw new BaseException(ErrorCode.INVALID_REQUEST_PASSWORD_POLICY);
    }
  }

  private void validateDuplicatedUser(String email, String nickname) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
      throw new BaseException(ErrorCode.EMAIL_CONFLICT);
    }
  }
}
