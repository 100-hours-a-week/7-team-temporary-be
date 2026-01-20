package molip.server.s3;

import java.time.OffsetDateTime;

public record PresignedUrlResult(String url, OffsetDateTime expiresAt) {}
