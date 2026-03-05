package molip.server.schedule.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.schedule.event.ScheduleActionLoggedEvent;
import molip.server.schedule.service.ScheduleActionLogService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ScheduleActionLoggedEventHandler {

    private final ScheduleActionLogService scheduleActionLogService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ScheduleActionLoggedEvent event) {
        scheduleActionLogService.save(
                event.userId(), event.scheduleId(), event.actionType(), event.apiPath());
    }
}
