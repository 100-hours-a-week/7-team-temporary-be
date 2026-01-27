package molip.server.schedule.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.repository.DayPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayPlanService {
    private final DayPlanRepository dayPlanRepository;

    @Transactional(readOnly = true)
    public DayPlan getDayPlan(Long userId, Long dayPlanId) {

        return dayPlanRepository
                .findByIdAndUserIdAndDeletedAtIsNull(dayPlanId, userId)
                .orElseThrow(() -> new BaseException(ErrorCode.DAYPLAN_NOT_FOUND));
    }
}
