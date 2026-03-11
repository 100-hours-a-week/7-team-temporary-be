package molip.server.chat.service.video;

import java.time.Instant;

public record IssuedLiveKitToken(String accessToken, Instant expiresAt) {

    public static IssuedLiveKitToken of(String accessToken, Instant expiresAt) {
        return new IssuedLiveKitToken(accessToken, expiresAt);
    }
}
