package molip.server.schedule.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ScheduleType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.dto.request.ScheduleCreateRequest;
import molip.server.schedule.dto.response.ScheduleCreateResponse;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.repository.DayPlanRepository;
import molip.server.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final DayPlanRepository dayPlanRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public ScheduleCreateResponse createSchedule(Long dayPlanId, ScheduleCreateRequest request) {
        DayPlan dayPlan =
                dayPlanRepository
                        .findByIdAndDeletedAtIsNull(dayPlanId)
                        .orElseThrow(() -> new BaseException(ErrorCode.DAYPLAN_NOT_FOUND));

        Schedule schedule;
        if (request.type() == ScheduleType.FIXED) {

            LocalTime startAt = parseTime(request.startAt());
            LocalTime endAt = parseTime(request.endAt());

            if (startAt != null && endAt != null) {
                if (scheduleRepository.existsTimeOverlap(dayPlanId, startAt, endAt)) {
                    throw new BaseException(ErrorCode.CONFLICT_TIME_OVERLAP);
                }
            }

            schedule =
                    Schedule.builder()
                            .dayPlan(dayPlan)
                            .title(request.title())
                            .startAt(startAt)
                            .endAt(endAt)
                            .build();

        } else {

            schedule =
                    Schedule.builder()
                            .dayPlan(dayPlan)
                            .title(request.title())
                            .estimatedTimeRange(request.estimatedTimeRange())
                            .focusLevel(request.focusLevel())
                            .isUrgent(request.isUrgent())
                            .build();
        }
        Schedule saved = scheduleRepository.save(schedule);

        return new ScheduleCreateResponse(saved.getId());
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_TIME_FORMAT);
        }
    }
}
