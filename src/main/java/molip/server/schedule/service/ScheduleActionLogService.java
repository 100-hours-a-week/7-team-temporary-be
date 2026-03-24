package molip.server.schedule.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.outbox.core.service.OutboxEventService;
import molip.server.schedule.entity.ScheduleActionLog;
import molip.server.schedule.enums.ScheduleActionType;
import molip.server.schedule.event.ScheduleActionLoggedEvent;
import molip.server.schedule.repository.ScheduleActionLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleActionLogService {

    private final ScheduleActionLogRepository scheduleActionLogRepository;
    private final OutboxEventService outboxEventService;
    private final ApplicationEventPublisher eventPublisher;

    public void publish(
            Long userId, Long scheduleId, ScheduleActionType actionType, String apiPath) {
        if (userId == null || actionType == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        eventPublisher.publishEvent(
                new ScheduleActionLoggedEvent(userId, scheduleId, actionType, apiPath));
    }

    @Transactional
    public void save(Long userId, Long scheduleId, ScheduleActionType actionType, String apiPath) {
        if (userId == null || actionType == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ScheduleActionLog actionLog =
                scheduleActionLogRepository.save(
                        new ScheduleActionLog(userId, scheduleId, actionType, apiPath));

        outboxEventService.recordCreated(
                AggregateType.SCHEDULE_ACTION_LOG,
                actionLog.getId(),
                OutboxPayloadMapper.scheduleActionLog(actionLog));
    }
}
