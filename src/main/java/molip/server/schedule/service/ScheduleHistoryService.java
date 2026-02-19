package molip.server.schedule.service;

import lombok.RequiredArgsConstructor;
import molip.server.migration.event.AggregateType;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.schedule.entity.ScheduleHistory;
import molip.server.schedule.repository.ScheduleHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleHistoryService {

    private final ScheduleHistoryRepository scheduleHistoryRepository;
    private final OutboxEventService outboxEventService;

    @Transactional
    public void saveHistory(ScheduleHistory history) {

        ScheduleHistory savedHistory = scheduleHistoryRepository.save(history);
        outboxEventService.recordCreated(AggregateType.SCHEDULE_HISTORY, savedHistory.getId());
    }
}
