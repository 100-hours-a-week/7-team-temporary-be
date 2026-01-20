package molip.server.user.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Users extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;

  private String password;

  private String nickname;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private LocalDate birth;

  @Enumerated(EnumType.STRING)
  private FocusTimeZone focusTimeZone;

  private LocalDateTime dayEndTime;
}
