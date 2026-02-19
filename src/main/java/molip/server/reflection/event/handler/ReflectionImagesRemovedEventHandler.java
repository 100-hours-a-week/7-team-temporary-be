package molip.server.reflection.event.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.image.entity.Image;
import molip.server.image.service.ImageService;
import molip.server.migration.event.AggregateType;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.event.ReflectionImagesRemovedEvent;
import molip.server.reflection.repository.DayReflectionImageRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReflectionImagesRemovedEventHandler {

    private final DayReflectionImageRepository dayReflectionImageRepository;
    private final ImageService imageService;
    private final OutboxEventService outboxEventService;

    @EventListener
    public void handle(ReflectionImagesRemovedEvent event) {
        List<Long> removeImageIds = event.removeImageIds();
        if (removeImageIds == null || removeImageIds.isEmpty()) {
            return;
        }

        List<DayReflectionImage> existing =
                dayReflectionImageRepository.findByReflectionIdWithImage(event.reflectionId());

        for (DayReflectionImage item : existing) {
            Image image = item.getImage();
            if (removeImageIds.contains(image.getId())) {
                item.delete();
                image.deleteImage();
                imageService.deleteStoredImage(image.getImageType(), image.getUploadKey());
                outboxEventService.recordDeleted(AggregateType.REFLECTION_IMAGE, item.getId());
                outboxEventService.recordDeleted(AggregateType.IMAGE, image.getId());
            }
        }
    }
}
