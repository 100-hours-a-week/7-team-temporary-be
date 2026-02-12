package molip.server.reflection.repository;

import java.util.Optional;
import molip.server.reflection.entity.ReflectionLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionLikeRepository extends JpaRepository<ReflectionLike, Long> {

    boolean existsByUserIdAndReflectionIdAndDeletedAtIsNull(Long userId, Long reflectionId);

    Optional<ReflectionLike> findByUserIdAndReflectionIdAndDeletedAtIsNull(
            Long userId, Long reflectionId);

    java.util.List<ReflectionLike> findByUserIdAndReflectionIdInAndDeletedAtIsNull(
            Long userId, java.util.List<Long> reflectionIds);
}
