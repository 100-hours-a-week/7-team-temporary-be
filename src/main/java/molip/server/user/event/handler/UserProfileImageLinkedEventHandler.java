package molip.server.user.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.user.event.UserProfileImageLinkedEvent;
import molip.server.user.facade.UserCommandFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileImageLinkedEventHandler {
    private final UserCommandFacade userCommandFacade;

    @EventListener
    public void handle(UserProfileImageLinkedEvent event) {
        userCommandFacade.linkProfileImage(event.userId(), event.imageKey());
    }
}
