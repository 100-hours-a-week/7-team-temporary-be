package molip.server.reflection.event;

import java.util.List;
import molip.server.image.entity.Image;
import molip.server.reflection.entity.DayReflection;

public record ReflectionImagesAddedEvent(DayReflection reflection, List<Image> images) {}
