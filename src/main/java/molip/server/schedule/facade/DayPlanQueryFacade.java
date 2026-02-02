package molip.server.schedule.facade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.repository.DayPlanRepository;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DayPlanQueryFacade {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DayPlanRepository dayPlanRepository;
    private final UserRepository userRepository;

    @Transactional
    public DayPlan getOrCreateDayPlan(Long userId, String date) {

        LocalDate planDate = parseDate(date);

        return dayPlanRepository
                .findByUserIdAndPlanDateAndDeletedAtIsNull(userId, planDate)
                .orElseGet(() -> createDayPlan(userId, planDate));
    }

    private LocalDate parseDate(String date) {

        if (date == null || date.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DATE_REQUIRED);
        }

        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DATE_REQUIRED);
        }
    }

    private DayPlan createDayPlan(Long userId, LocalDate planDate) {

        Users user =
                userRepository
                        .findByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return dayPlanRepository.save(new DayPlan(user, planDate));
    }
}
