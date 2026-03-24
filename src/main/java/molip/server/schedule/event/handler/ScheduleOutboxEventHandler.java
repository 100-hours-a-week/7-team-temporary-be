package molip.server.schedule.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.ChangeType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.outbox.core.service.OutboxEventService;
import molip.server.schedule.event.ScheduleOutboxEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ScheduleOutboxEventHandler {

    private final OutboxEventService outboxEventService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ScheduleOutboxEvent event) {
        if (event == null || event.schedule() == null || event.changeType() == null) {
            return;
        }

        if (event.changeType() == ChangeType.CREATED) {
            outboxEventService.recordCreated(
                    AggregateType.SCHEDULE,
                    event.schedule().getId(),
                    OutboxPayloadMapper.schedule(event.schedule()));
            return;
        }

        if (event.changeType() == ChangeType.UPDATED) {
            outboxEventService.recordUpdated(
                    AggregateType.SCHEDULE,
                    event.schedule().getId(),
                    OutboxPayloadMapper.schedule(event.schedule()));
            return;
        }

        if (event.changeType() == ChangeType.DELETED) {
            outboxEventService.recordDeleted(
                    AggregateType.SCHEDULE,
                    event.schedule().getId(),
                    OutboxPayloadMapper.schedule(event.schedule()));
        }
    }
}
