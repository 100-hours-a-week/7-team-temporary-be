package molip.server.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.common.enums.ImageType;
import molip.server.common.enums.UploadStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String uploadKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType imageType;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    public Image(
            String uploadKey,
            UploadStatus uploadStatus,
            ImageType imageType,
            OffsetDateTime expiresAt) {
        this.uploadKey = uploadKey;
        this.uploadStatus = uploadStatus;
        this.imageType = imageType;
        this.expiresAt = expiresAt;
    }

    public void markSuccess() {
        this.uploadStatus = UploadStatus.SUCCESS;
    }

    public void deleteImage() {
        this.deletedAt = LocalDateTime.now();
    }
}
