package molip.server.reflection.event;

import java.util.List;

public record ReflectionImagesRemovedEvent(Long reflectionId, List<Long> removeImageIds) {}
