package molip.server.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 비활성화 요청")
public record UserFcmTokenDeactivateRequest(
        @Schema(description = "FCM 토큰", example = "fcm-token-value") @NotBlank String fcmToken) {}
