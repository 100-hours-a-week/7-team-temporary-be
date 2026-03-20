package molip.server.chat.service.video;

import java.time.Instant;

public record WebRtcToken(String webrtcRoomName, String accessToken, Instant expiresAt) {

    public static WebRtcToken of(String webrtcRoomName, String accessToken, Instant expiresAt) {
        return new WebRtcToken(webrtcRoomName, accessToken, expiresAt);
    }
}
