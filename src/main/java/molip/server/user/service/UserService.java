package molip.server.user.service;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

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
    // TODO validation 코드 추가
    String encodedPassword = passwordEncoder.encode(password);

    return null;
  }
}
