package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.response.ImageInfoResponse;

@Schema(description = "채팅 메시지 항목")
public record ChatMessageItemResponse(
    @Schema(description = "메시지 ID", example = "210") Long messageId,
    @Schema(description = "메시지 타입", example = "TEXT") MessageType messageType,
    @Schema(description = "발신자 타입", example = "USER") SenderType senderType,
    @Schema(description = "발신자 ID", example = "3") Long senderId,
    @Schema(description = "내용", example = "오늘 스터디 몇 시에 시작해?") String content,
    @Schema(description = "이미지 목록") List<ImageInfoResponse> images,
    @Schema(description = "전송 시각", example = "2026-01-13T19:20:10+09:00") OffsetDateTime sentAt) {}
