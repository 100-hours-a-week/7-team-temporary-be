package molip.server.schedule.service.strategy;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.enums.ScheduleType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FixedScheduleCreator implements ScheduleCreator {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleTimeParser timeParser;

    @Override
    public ScheduleType supports() {
        return ScheduleType.FIXED;
    }

    @Override
    public Schedule create(
            DayPlan dayPlan,
            String title,
            LocalTime startAt,
            LocalTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {

        LocalTime startAtValue = timeParser.parse(startAt);
        LocalTime endAtValue = timeParser.parse(endAt);

        if (startAtValue == null || endAtValue == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        if (!endAtValue.isAfter(startAtValue)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_TIME_RANGE);
        }

        if (scheduleRepository.existsTimeOverlap(dayPlan.getId(), startAtValue, endAtValue)) {
            throw new BaseException(ErrorCode.CONFLICT_TIME_OVERLAP);
        }

        return Schedule.builder()
                .dayPlan(dayPlan)
                .title(title)
                .status(ScheduleStatus.TODO)
                .type(ScheduleType.FIXED)
                .assignedBy(AssignedBy.USER)
                .assignmentStatus(AssignmentStatus.ASSIGNED)
                .startAt(startAtValue)
                .endAt(endAtValue)
                .build();
    }
}
