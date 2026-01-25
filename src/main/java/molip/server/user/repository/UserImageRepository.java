package molip.server.user.repository;

import java.util.Optional;
import molip.server.user.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    Optional<UserImage> findTopByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}
