package molip.server.reflection.event.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.image.entity.Image;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.event.ReflectionImagesAddedEvent;
import molip.server.reflection.repository.DayReflectionImageRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReflectionImagesAddedEventHandler {

    private final DayReflectionImageRepository dayReflectionImageRepository;

    @EventListener
    public void handle(ReflectionImagesAddedEvent event) {
        List<Image> images = event.images();
        if (images == null || images.isEmpty()) {
            return;
        }

        for (Image image : images) {
            image.markSuccess();
            dayReflectionImageRepository.save(new DayReflectionImage(event.reflection(), image));
        }
    }
}
