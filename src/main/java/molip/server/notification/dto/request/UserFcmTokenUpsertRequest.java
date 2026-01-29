package molip.server.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import molip.server.common.enums.Platform;

@Schema(description = "FCM 토큰 등록/갱신 요청")
public record UserFcmTokenUpsertRequest(
        @Schema(description = "FCM 토큰", example = "fcm-token-value") @NotBlank String fcmToken,
        @Schema(description = "플랫폼", example = "WEB") @NotNull Platform platform) {}
