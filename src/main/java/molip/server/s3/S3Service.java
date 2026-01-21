package molip.server.s3;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {
  private final S3Properties s3Properties;
  private final S3Presigner preSigner;

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

  private GetObjectPresignRequest getObjectPresignRequest(String key, Duration duration) {
    return GetObjectPresignRequest.builder()
        .signatureDuration(duration)
        .getObjectRequest(objectRequest -> objectRequest.bucket(s3Properties.bucket()).key(key))
        .build();
  }

  private PutObjectPresignRequest putObjectPresignRequest(String key, Duration duration) {
    return PutObjectPresignRequest.builder()
        .signatureDuration(duration)
        .putObjectRequest(objectRequest -> objectRequest.bucket(s3Properties.bucket()).key(key))
        .build();
  }
}
