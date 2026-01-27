package molip.server.image.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.ImageType;
import molip.server.common.enums.UploadStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.image.dto.response.ImageGetUrlResponse;
import molip.server.image.dto.response.ImageUploadUrlResponse;
import molip.server.image.entity.Image;
import molip.server.image.repository.ImageRepository;
import molip.server.s3.PresignedUrlResult;
import molip.server.s3.S3Service;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static final Duration DEFAULT_EXPIRATION = Duration.ofMinutes(5);

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    public ImageUploadUrlResponse issueUploadUrl(ImageType type) {
        String imageKey = UUID.randomUUID().toString();
        String objectKey = resolveObjectKey(type, imageKey);
        PresignedUrlResult result = s3Service.createPutPresignedUrl(objectKey, DEFAULT_EXPIRATION);
        imageRepository.save(new Image(imageKey, UploadStatus.PENDING, type, result.expiresAt()));
        return new ImageUploadUrlResponse(result.url(), result.expiresAt(), imageKey);
    }

    public ImageGetUrlResponse issueGetUrl(ImageType type, String imageKey) {
        imageRepository
                .findByUploadKeyAndDeletedAtIsNull(imageKey)
                .orElseThrow(() -> new BaseException(ErrorCode.IMAGE_NOT_FOUND));
        String objectKey = resolveObjectKey(type, imageKey);
        PresignedUrlResult result = s3Service.createGetPresignedUrl(objectKey, DEFAULT_EXPIRATION);
        return new ImageGetUrlResponse(result.url(), result.expiresAt(), imageKey);
    }

    public ImageGetUrlResponse issueGetUrlWithoutValidation(ImageType type, String imageKey) {
        String objectKey = resolveObjectKey(type, imageKey);
        PresignedUrlResult result = s3Service.createGetPresignedUrl(objectKey, DEFAULT_EXPIRATION);
        return new ImageGetUrlResponse(result.url(), result.expiresAt(), imageKey);
    }

    public void deleteStoredImage(ImageType type, String imageKey) {
        String objectKey = resolveObjectKey(type, imageKey);
        s3Service.deleteObject(objectKey);
    }

    private String resolveObjectKey(ImageType type, String imageKey) {
        return type.folder() + "/" + imageKey;
    }
}
