package molip.server.user.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private LocalTime dayEndTime;

    public Users(
            String email,
            String password,
            String nickname,
            Gender gender,
            LocalDate birth,
            FocusTimeZone focusTimeZone,
            LocalTime dayEndTime) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
        this.focusTimeZone = focusTimeZone;
        this.dayEndTime = dayEndTime;
    }

    public void modifyUserDetails(
            Gender gender,
            LocalDate birth,
            FocusTimeZone focusTimeZone,
            LocalTime dayEndTime,
            String nickname) {
        this.gender = (gender != null) ? gender : this.gender;
        this.birth = (birth != null) ? birth : this.birth;
        this.focusTimeZone = (focusTimeZone != null) ? focusTimeZone : this.focusTimeZone;
        this.dayEndTime = (dayEndTime != null) ? dayEndTime : this.dayEndTime;
        this.nickname = (nickname != null) ? nickname : this.nickname;
    }

    public void modifyPassword(String password) {
        this.password = password;
    }

    public void deleteUser() {
        this.deletedAt = LocalDateTime.now();
    }
}
