package molip.server.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import molip.server.user.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
  @Autowired private UserRepository userRepository;

  @Test
  void 이메일이_존재하면_true를_반환한다() {
    // given
    Users user = createUser("email@test.com");
    userRepository.save(user);

    // when
    boolean exists = userRepository.existsByEmailAndDeletedAtIsNull("email@test.com");

    // then
    assertThat(exists).isTrue();
  }

  @Test
  void 삭제된_유저는_조회되지_않는다() {
    // given
    Users user = createUser("email@test.com");
    userRepository.save(user);
    ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
    userRepository.save(user);

    // when
    boolean exists = userRepository.existsByEmailAndDeletedAtIsNull("email@test.com");

    // then
    assertThat(exists).isFalse();
  }

  @Test
  void 이메일로_유저를_조회한다() {
    // given
    Users user = createUser("email@test.com");
    userRepository.save(user);

    // when
    var result = userRepository.findByEmailAndDeletedAtIsNull("email@test.com");

    // then
    assertThat(result).isPresent();
  }

  private Users createUser(String email) {
    return new Users(
        email,
        "encoded",
        "nick",
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        FocusTimeZone.MORNING,
        LocalTime.of(22, 40));
  }
}
