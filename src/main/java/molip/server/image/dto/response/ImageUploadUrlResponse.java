package molip.server.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "이미지 업로드 URL 응답")
public record ImageUploadUrlResponse(
        @Schema(description = "업로드 URL", example = "https://s3....") String uploadUrl,
        @Schema(description = "만료 시각", example = "2026-01-13T11:10:00+09:00")
                OffsetDateTime expiresAt,
        @Schema(description = "이미지 키", example = "550e8400-e29b-41d4-a716-446655440000")
                String imageKey) {}
