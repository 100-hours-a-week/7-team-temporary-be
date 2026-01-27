package molip.server.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalTime;
import molip.server.common.enums.FocusTimeZone;
import molip.server.common.enums.Gender;

@Schema(description = "회원 정보 수정 요청")
public record UpdateUserRequest(
        @Nullable @Schema(description = "성별", example = "MALE") Gender gender,
        @Nullable
                @Schema(description = "생년월일", example = "1990.01.01")
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
                LocalDate birth,
        @Nullable @Schema(description = "집중 시간대", example = "MORNING") FocusTimeZone focusTimeZone,
        @Nullable
                @Schema(description = "하루 종료 기준", example = "22:40")
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
                LocalTime dayEndTime,
        @Nullable @Schema(description = "닉네임", example = "nick") String nickname) {}
