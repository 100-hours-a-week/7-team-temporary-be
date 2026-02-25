package molip.server.reflection.dto.cache;

import java.util.List;
import molip.server.reflection.entity.DayReflection;

public record ReflectionCachePayload(
        Long reflectionId,
        Long userId,
        long version,
        boolean isOpen,
        String title,
        String content,
        String createdAt,
        String ownerNickname,
        List<String> imageKeys,
        String deletedAt) {

    public static ReflectionCachePayload from(
            DayReflection reflection, List<String> imageKeys, String ownerNickname) {
        return new ReflectionCachePayload(
                reflection.getId(),
                reflection.getUser().getId(),
                resolveVersion(reflection.getVersion()),
                reflection.isOpen(),
                reflection.getTitle(),
                reflection.getContent(),
                reflection.getCreatedAt() == null ? null : reflection.getCreatedAt().toString(),
                ownerNickname,
                imageKeys,
                reflection.getDeletedAt() == null ? null : reflection.getDeletedAt().toString());
    }

    private static long resolveVersion(Long version) {
        return version == null ? 0L : Math.max(0L, version);
    }
}
