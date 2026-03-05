package molip.server.report.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.report.event.ReportChatRespondRequestedEvent;
import molip.server.report.facade.ReportChatStreamFacade;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportChatRespondRequestedEventHandler {

    private final ReportChatStreamFacade reportChatStreamFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReportChatRespondRequestedEvent event) {
        reportChatStreamFacade.startStream(event.reportId(), event.messageId());
    }
}
