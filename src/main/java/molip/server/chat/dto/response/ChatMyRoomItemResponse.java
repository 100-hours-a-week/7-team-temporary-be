package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import molip.server.chat.entity.ChatRoom;
import molip.server.common.enums.ChatRoomType;

@Schema(description = "내 채팅방 조회 항목")
public record ChatMyRoomItemResponse(
        @Schema(description = "채팅방 ID", example = "10") Long roomId,
        @Schema(description = "타입", example = "OPEN_CHAT") ChatRoomType type,
        @Schema(description = "제목", example = "삼전 적정가는 18만이다.") String title,
        @Schema(description = "설명", example = "삼전이 18만원이 적정가인가에 대한 토론방") String description,
        @Schema(description = "최대 인원", example = "10") int maxParticipants,
        @Schema(description = "참여자 수", example = "2") int participantsCount,
        @Schema(description = "안 읽은 메시지 수", example = "3") int unreadCount,
        @Schema(description = "마지막 메시지 미리보기", example = "안녕하세요") String lastMessagePreview,
        @Schema(description = "마지막 메시지 전송 시각", example = "2026-01-13T19:20:10+09:00")
                OffsetDateTime lastMessageSentAt) {

    public static ChatMyRoomItemResponse of(
            ChatRoom chatRoom,
            int participantsCount,
            int unreadCount,
            String lastMessagePreview,
            OffsetDateTime lastMessageSentAt) {
        return new ChatMyRoomItemResponse(
                chatRoom.getId(),
                chatRoom.getType(),
                chatRoom.getTitle(),
                chatRoom.getDescription(),
                chatRoom.getMaxParticipants(),
                participantsCount,
                unreadCount,
                lastMessagePreview,
                lastMessageSentAt);
    }
}
