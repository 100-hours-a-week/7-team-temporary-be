package molip.server.schedule.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.repository.DayPlanRepository;
import molip.server.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
    private final DayPlanRepository dayPlanRepository;
    private final ScheduleRepository scheduleRepository;
    private final Map<ScheduleType, ScheduleCreator> creatorMap;

    public ScheduleService(
            DayPlanRepository dayPlanRepository,
            ScheduleRepository scheduleRepository,
            List<ScheduleCreator> creators) {
        this.dayPlanRepository = dayPlanRepository;
        this.scheduleRepository = scheduleRepository;
        this.creatorMap = new EnumMap<>(ScheduleType.class);
        for (ScheduleCreator creator : creators) {
            this.creatorMap.put(creator.supports(), creator);
        }
    }

    @Transactional
    public Schedule createSchedule(
            Long userId,
            Long dayPlanId,
            ScheduleType type,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {

        DayPlan dayPlan =
                dayPlanRepository
                        .findByIdAndUserIdAndDeletedAtIsNull(dayPlanId, userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.DAYPLAN_NOT_FOUND));

        ScheduleCreator creator = resolveCreator(type);
        Schedule schedule =
                creator.create(
                        dayPlan, title, startAt, endAt, estimatedTimeRange, focusLevel, isUrgent);

        return scheduleRepository.save(schedule);
    }

    private ScheduleCreator resolveCreator(ScheduleType type) {
        if (type == null || !creatorMap.containsKey(type)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        return Objects.requireNonNull(creatorMap.get(type));
    }
}
