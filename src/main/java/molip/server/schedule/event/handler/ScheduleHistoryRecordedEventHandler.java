package molip.server.schedule.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.schedule.event.ScheduleHistoryRecordedEvent;
import molip.server.schedule.service.ScheduleHistoryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ScheduleHistoryRecordedEventHandler {

    private final ScheduleHistoryService scheduleHistoryService;

    @TransactionalEventListener
    public void handle(ScheduleHistoryRecordedEvent event) {

        scheduleHistoryService.saveHistory(event.history());
    }
}
