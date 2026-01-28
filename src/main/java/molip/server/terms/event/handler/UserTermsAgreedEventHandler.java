package molip.server.terms.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.terms.event.UserTermsAgreedEvent;
import molip.server.terms.facade.TermsSignCommandFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTermsAgreedEventHandler {

    private final TermsSignCommandFacade termsSignCommandFacade;

    @EventListener
    public void handleUserTermsAgreed(UserTermsAgreedEvent event) {

        termsSignCommandFacade.createTermsSigns(event.userId(), event.terms());
    }
}
