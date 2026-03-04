package molip.server.report.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.report.event.ReportChatRespondRequestedEvent;
import molip.server.report.service.ReportChatStreamService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportChatRespondRequestedEventHandler {

    private final ReportChatStreamService reportChatStreamService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReportChatRespondRequestedEvent event) {
        reportChatStreamService.startStream(event.reportId(), event.messageId());
    }
}
