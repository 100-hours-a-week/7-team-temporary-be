package molip.server.reflection.event.handler;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.image.entity.Image;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.event.DayReflectionImagesCreateEvent;
import molip.server.reflection.repository.DayReflectionImageRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DayReflectionImagesCreateEventHandler {

    private final DayReflectionImageRepository dayReflectionImageRepository;

    @EventListener
    public void handle(DayReflectionImagesCreateEvent event) {
        DayReflection reflection = event.reflection();
        List<Image> images = event.images();

        List<DayReflectionImage> reflectionImages = new ArrayList<>();
        for (Image image : images) {
            image.markSuccess();
            reflectionImages.add(new DayReflectionImage(reflection, image));
        }
        dayReflectionImageRepository.saveAll(reflectionImages);
    }
}
