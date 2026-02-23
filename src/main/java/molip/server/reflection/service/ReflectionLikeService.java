package molip.server.reflection.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.ReflectionLike;
import molip.server.reflection.repository.ReflectionLikeRepository;
import molip.server.user.entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReflectionLikeService {

    private final ReflectionLikeRepository reflectionLikeRepository;

    @Transactional(readOnly = true)
    public boolean isAlreadyLiked(Long userId, Long reflectionId) {
        return reflectionLikeRepository.existsByUserIdAndReflectionIdAndDeletedAtIsNull(
                userId, reflectionId);
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long reflectionId) {
        return reflectionLikeRepository.existsByUserIdAndReflectionIdAndDeletedAtIsNull(
                userId, reflectionId);
    }

    @Transactional(readOnly = true)
    public Set<Long> findLikedReflectionIds(Long userId, List<Long> reflectionIds) {
        if (reflectionIds == null || reflectionIds.isEmpty()) {
            return Collections.emptySet();
        }
        return reflectionLikeRepository
                .findByUserIdAndReflectionIdInAndDeletedAtIsNull(userId, reflectionIds)
                .stream()
                .map(item -> item.getReflection().getId())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void save(Users user, DayReflection reflection) {
        reflectionLikeRepository.save(new ReflectionLike(user, reflection));
    }
}
