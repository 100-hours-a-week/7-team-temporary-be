package molip.server.reflection.event.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.image.entity.Image;
import molip.server.migration.event.AggregateType;
import molip.server.migration.event.OutboxPayloadMapper;
import molip.server.migration.outbox.OutboxEventService;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.event.ReflectionImagesAddedEvent;
import molip.server.reflection.repository.DayReflectionImageRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReflectionImagesAddedEventHandler {

    private final DayReflectionImageRepository dayReflectionImageRepository;
    private final OutboxEventService outboxEventService;

    @EventListener
    public void handle(ReflectionImagesAddedEvent event) {
        List<Image> images = event.images();
        if (images == null || images.isEmpty()) {
            return;
        }

        for (Image image : images) {
            image.markSuccess();
            DayReflectionImage savedImage =
                    dayReflectionImageRepository.save(
                            new DayReflectionImage(event.reflection(), image));
            outboxEventService.recordCreated(
                    AggregateType.REFLECTION_IMAGE,
                    savedImage.getId(),
                    OutboxPayloadMapper.reflectionImage(savedImage));
            outboxEventService.recordUpdated(
                    AggregateType.IMAGE, image.getId(), OutboxPayloadMapper.image(image));
        }
    }
}
