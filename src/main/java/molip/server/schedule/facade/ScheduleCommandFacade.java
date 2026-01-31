package molip.server.schedule.facade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.schedule.dto.request.ScheduleChildrenCreateItem;
import molip.server.schedule.dto.response.ScheduleChildrenCreateGroupResponse;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.service.ScheduleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScheduleCommandFacade {

    private final ScheduleService scheduleService;

    @Transactional
    public List<ScheduleChildrenCreateGroupResponse> createChildrenBatch(
            Long userId, List<ScheduleChildrenCreateItem> schedules) {

        validateChildrenBatchRequest(schedules);

        List<Schedule> results = new ArrayList<>();
        HashSet<Long> parentScheduleIds = new HashSet<>();

        for (ScheduleChildrenCreateItem scheduleRequest : schedules) {

            validateChildrenBatchItem(scheduleRequest);
            validateDuplicateParentScheduleId(
                    parentScheduleIds, scheduleRequest.parentScheduleId());

            results.addAll(
                    scheduleService.createChildren(
                            userId, scheduleRequest.parentScheduleId(), scheduleRequest.titles()));
        }

        return ScheduleChildrenCreateGroupResponse.groupFrom(results);
    }

    private void validateChildrenBatchRequest(List<ScheduleChildrenCreateItem> schedules) {

        if (schedules == null || schedules.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private void validateChildrenBatchItem(ScheduleChildrenCreateItem scheduleRequest) {

        if (scheduleRequest == null
                || scheduleRequest.parentScheduleId() == null
                || scheduleRequest.titles() == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }
    }

    private void validateDuplicateParentScheduleId(
            HashSet<Long> parentScheduleIds, Long parentScheduleId) {

        if (!parentScheduleIds.add(parentScheduleId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DUPLICATED_PARENT_SCHEDULE);
        }
    }
}
