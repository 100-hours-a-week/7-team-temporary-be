package molip.server.schedule.facade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.migration.event.AggregateType;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.schedule.dto.response.DayPlanScheduleExistResponse;
import molip.server.schedule.dto.response.DayPlanScheduleExistResponse.DayPlanScheduleExistItem;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.repository.DayPlanRepository;
import molip.server.schedule.repository.ScheduleRepository;
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
    private final ScheduleRepository scheduleRepository;
    private final OutboxEventService outboxEventService;

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

        DayPlan dayPlan = dayPlanRepository.save(new DayPlan(user, planDate));
        outboxEventService.recordCreated(AggregateType.DAY_PLAN, dayPlan.getId());
        return dayPlan;
    }

    @Transactional(readOnly = true)
    public DayPlanScheduleExistResponse getDayPlanExistence(
            Long userId, String startDate, String endDate) {

        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        if (start.isAfter(end)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DATE_REQUIRED);
        }

        Set<LocalDate> hasPlans =
                new HashSet<>(
                        scheduleRepository.findPlanDatesWithTimeAssignedSchedules(
                                userId, start, end));

        List<DayPlanScheduleExistItem> days = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            days.add(new DayPlanScheduleExistItem(cursor, hasPlans.contains(cursor)));
            cursor = cursor.plusDays(1);
        }

        return DayPlanScheduleExistResponse.of(start, end, days);
    }
}
