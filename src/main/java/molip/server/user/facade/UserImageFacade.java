package molip.server.user.facade;

import molip.server.user.event.UserProfileImageLinkedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserImageFacade {
  @EventListener
  public void handleUserProfileImageLinked(UserProfileImageLinkedEvent event) {}
}
