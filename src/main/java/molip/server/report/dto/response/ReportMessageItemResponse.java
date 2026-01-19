package molip.server.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

@Schema(description = "리포트 메시지 항목")
public record ReportMessageItemResponse(
    @Schema(description = "메시지 ID", example = "180") Long messageId,
    @Schema(description = "발신자 타입", example = "USER") SenderType senderType,
    @Schema(description = "메시지 타입", example = "TEXT") MessageType messageType,
    @Schema(description = "메시지 내용", example = "다음주에 뭐부터 하면 좋을까?") String content,
    @Schema(description = "전송 시각", example = "2026-01-18T23:40:10+09:00") OffsetDateTime sentAt) {}
