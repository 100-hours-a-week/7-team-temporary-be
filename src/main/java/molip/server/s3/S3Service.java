package molip.server.s3;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {
  private static final String MOLIP_IMAGE_FOLDER = "molip";

  private final S3Properties s3Properties;
  private final S3Presigner preSigner;

  S3Service(S3Properties s3Properties, S3Presigner preSigner) {
    this.s3Properties = s3Properties;
    this.preSigner = preSigner;
  }

  public String getCommunityImage(Long imageId) {
    return getPreSignedUrl(MOLIP_IMAGE_FOLDER, imageId.toString());
  }

  public String getCommunityImageUploadUrl(Long imageId) {
    return getPreSignedPutUrl(MOLIP_IMAGE_FOLDER, imageId.toString());
  }

  public PresignedUrlResult createGetPresignedUrl(String key, Duration duration) {
    PresignedGetObjectRequest presigned =
        preSigner.presignGetObject(getObjectPresignRequest(key, duration));
    return new PresignedUrlResult(
        presigned.url().toString(),
        OffsetDateTime.ofInstant(presigned.expiration(), ZoneId.of("Asia/Seoul")));
  }

  public PresignedUrlResult createPutPresignedUrl(String key, Duration duration) {
    PresignedPutObjectRequest presigned =
        preSigner.presignPutObject(putObjectPresignRequest(key, duration));
    return new PresignedUrlResult(
        presigned.url().toString(),
        OffsetDateTime.ofInstant(presigned.expiration(), ZoneId.of("Asia/Seoul")));
  }

  private String getPreSignedUrl(String folder, String filename) {
    return preSigner.presignGetObject(getObjectPresignRequest(folder, filename)).url().toString();
  }

  private GetObjectPresignRequest getObjectPresignRequest(String folder, String filename) {
    return GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(1))
        .getObjectRequest(
            objectRequest ->
                objectRequest.bucket(s3Properties.bucket()).key(String.join("/", folder, filename)))
        .build();
  }

  private GetObjectPresignRequest getObjectPresignRequest(String key, Duration duration) {
    return GetObjectPresignRequest.builder()
        .signatureDuration(duration)
        .getObjectRequest(objectRequest -> objectRequest.bucket(s3Properties.bucket()).key(key))
        .build();
  }

  private String getPreSignedPutUrl(String folder, String filename) {
    return preSigner.presignPutObject(putObjectPresignRequest(folder, filename)).url().toString();
  }

  private PutObjectPresignRequest putObjectPresignRequest(String folder, String filename) {
    return PutObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(1))
        .putObjectRequest(
            objectRequest ->
                objectRequest.bucket(s3Properties.bucket()).key(String.join("/", folder, filename)))
        .build();
  }

  private PutObjectPresignRequest putObjectPresignRequest(String key, Duration duration) {
    return PutObjectPresignRequest.builder()
        .signatureDuration(duration)
        .putObjectRequest(objectRequest -> objectRequest.bucket(s3Properties.bucket()).key(key))
        .build();
  }
}
