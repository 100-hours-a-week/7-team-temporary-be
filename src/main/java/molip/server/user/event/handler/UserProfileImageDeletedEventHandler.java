package molip.server.user.event.handler;

import lombok.RequiredArgsConstructor;
import molip.server.image.service.ImageService;
import molip.server.user.event.UserProfileImageDeletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserProfileImageDeletedEventHandler {
    private final ImageService imageService;

    @TransactionalEventListener
    public void handle(UserProfileImageDeletedEvent event) {
        imageService.deleteStoredImage(event.imageType(), event.imageKey());
    }
}
