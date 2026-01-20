package molip.server.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "회원가입 요청")
public record SignUpRequest(
    @Schema(description = "이메일", example = "email@email.com") String email,
    @Schema(description = "비밀번호", example = "password") String password,
    @Schema(description = "닉네임", example = "nickname") String nickname,
    @Schema(description = "성별", example = "MALE") Gender gender,
    @Schema(description = "생년월일", example = "1990.01.01")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate birth,
    @Schema(description = "집중 시간대", example = "MORNING") FocusTimeZone focusTimeZone,
    @Schema(description = "하루 종료 기준", example = "22:40")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @DateTimeFormat(pattern = "HH:mm")
        LocalTime dayEndTime,
    @Schema(description = "프로필 이미지 키", example = "550e8400-e29b-41d4-a716-446655440000")
        String profileImageKey) {}
