package molip.server.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "WebRTC 토큰 발급 응답")
public record WebRtcTokenIssueResponse(
        @Schema(description = "채팅방 ID", example = "25") Long roomId,
        @Schema(description = "채팅방 참가자 ID", example = "101") Long participantId,
        @Schema(description = "WebRTC room 이름", example = "chat-room-25") String webrtcRoomName,
        @Schema(description = "LiveKit access token", example = "lk_token_xxx") String accessToken,
        @Schema(description = "토큰 만료 시각(UTC)", example = "2026-03-10T13:00:00Z")
                Instant expiresAt) {

    public static WebRtcTokenIssueResponse of(
            Long roomId,
            Long participantId,
            String webrtcRoomName,
            String accessToken,
            Instant expiresAt) {
        return new WebRtcTokenIssueResponse(
                roomId, participantId, webrtcRoomName, accessToken, expiresAt);
    }
}
