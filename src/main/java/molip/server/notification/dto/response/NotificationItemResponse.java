package molip.server.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import molip.server.common.enums.NotificationStatus;
import molip.server.common.enums.NotificationType;

@Schema(description = "알림 항목")
public record NotificationItemResponse(
    @Schema(description = "알림 ID", example = "10") Long notificationId,
    @Schema(description = "알림 타입", example = "FRIEND_REQUESTED") NotificationType type,
    @Schema(description = "제목", example = "nick님이 친구 요청을 보냈습니다.") String title,
    @Schema(description = "내용", example = "친구 목록에서 확인해주세요!") String content,
    @Schema(description = "상태", example = "SENT") NotificationStatus status,
    @Schema(description = "예약 시간", example = "2026-01-13T18:19:50+09:00")
        OffsetDateTime scheduledAt,
    @Schema(description = "발송 시간", example = "2026-01-13T18:20:00+09:00") OffsetDateTime sentAt) {}
