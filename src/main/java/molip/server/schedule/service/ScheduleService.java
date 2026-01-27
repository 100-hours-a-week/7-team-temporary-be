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
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.schedule.service.strategy.ScheduleCreator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final Map<ScheduleType, ScheduleCreator> creatorMap;

    public ScheduleService(ScheduleRepository scheduleRepository, List<ScheduleCreator> creators) {
        this.scheduleRepository = scheduleRepository;
        this.creatorMap = new EnumMap<>(ScheduleType.class);
        for (ScheduleCreator creator : creators) {
            this.creatorMap.put(creator.supports(), creator);
        }
    }

    @Transactional
    public Schedule createSchedule(
            DayPlan dayPlan,
            ScheduleType type,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {

        ScheduleCreator creator = resolveCreator(type);

        Schedule schedule =
                creator.create(
                        dayPlan, title, startAt, endAt, estimatedTimeRange, focusLevel, isUrgent);

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Page<Schedule> getTimeAssignedSchedulesByDayPlan(Long dayPlanId, int page, int size) {

        validatePage(page, size);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("startAt").ascending());

        return scheduleRepository.findTimeAssignedByDayPlanId(dayPlanId, pageRequest);
    }

    private ScheduleCreator resolveCreator(ScheduleType type) {
        if (type == null || !creatorMap.containsKey(type)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
        return Objects.requireNonNull(creatorMap.get(type));
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }
}
