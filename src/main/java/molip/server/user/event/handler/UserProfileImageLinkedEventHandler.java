package molip.server.user.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.user.event.UserProfileImageLinkedEvent;
import molip.server.user.facade.UserImageFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileImageLinkedEventHandler {
  private final UserImageFacade userImageFacade;

  @EventListener
  public void handle(UserProfileImageLinkedEvent event) {
    userImageFacade.linkProfileImage(event.userId(), event.imageKey());
  }
}
