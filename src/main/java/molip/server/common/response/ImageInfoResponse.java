package molip.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "이미지 정보")
public record ImageInfoResponse(
    @Schema(description = "Presigned URL", example = "https://...presigned...") String url,
    @Schema(description = "만료 시각", example = "2026-01-13T11:10:00+09:00") OffsetDateTime expiresAt,
    @Schema(description = "이미지 키", example = "users/12/profile/a1b2.png") String key,
    @Schema(description = "정렬 순서", example = "1") Integer sortOrder) {}
