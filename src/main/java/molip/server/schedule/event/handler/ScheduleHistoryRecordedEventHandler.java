package molip.server.schedule.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.schedule.event.ScheduleHistoryRecordedEvent;
import molip.server.schedule.service.ScheduleHistoryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleHistoryRecordedEventHandler {

    private final ScheduleHistoryService scheduleHistoryService;

    @EventListener
    public void handle(ScheduleHistoryRecordedEvent event) {

        scheduleHistoryService.saveHistory(event.history());
    }
}
