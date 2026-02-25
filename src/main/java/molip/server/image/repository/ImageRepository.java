package molip.server.image.repository;

import java.util.List;
import java.util.Optional;
import molip.server.common.enums.UploadStatus;
import molip.server.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByUploadKeyAndDeletedAtIsNull(String uploadKey);

    Optional<Image> findByUploadKeyAndUploadStatusAndDeletedAtIsNull(
            String uploadKey, UploadStatus uploadStatus);

    List<Image> findByIdInAndDeletedAtIsNull(java.util.List<Long> ids);

    List<Image> findByUploadKeyInAndDeletedAtIsNull(java.util.List<String> uploadKeys);
}
