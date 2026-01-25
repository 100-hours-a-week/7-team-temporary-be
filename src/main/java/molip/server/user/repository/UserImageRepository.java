package molip.server.user.repository;

import java.util.Optional;
import molip.server.user.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    @Query(
            "select ui from UserImage ui join fetch ui.image "
                    + "where ui.user.id = :userId and ui.deletedAt is null "
                    + "order by ui.createdAt desc")
    Optional<UserImage> findLatestByUserIdWithImage(Long userId);
}
