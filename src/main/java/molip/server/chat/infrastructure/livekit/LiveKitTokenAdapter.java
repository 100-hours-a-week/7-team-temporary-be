package molip.server.chat.infrastructure.livekit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.config.LiveKitProperties;
import molip.server.chat.service.video.IssuedLiveKitToken;
import molip.server.chat.service.video.LiveKitTokenCommand;
import molip.server.chat.service.video.LiveKitTokenPort;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiveKitTokenAdapter implements LiveKitTokenPort {

    private final LiveKitProperties liveKitProperties;

    @Override
    public IssuedLiveKitToken issueToken(LiveKitTokenCommand command) {
        validate(command);
        validateProperties();
        log.info(
                "livekit jwt signing start: roomName={}, identity={}, ttlSeconds={}, livekitUrl={}",
                command.roomName(),
                command.identity(),
                command.ttlSeconds(),
                liveKitProperties.getUrl());

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(command.ttlSeconds());

        Map<String, Object> videoGrant = new HashMap<>();
        videoGrant.put("roomJoin", true);
        videoGrant.put("room", command.roomName());
        videoGrant.put("canPublish", command.canPublish());
        videoGrant.put("canSubscribe", command.canSubscribe());

        Map<String, Object> claims = new HashMap<>();
        claims.put("video", videoGrant);
        if (command.displayName() != null && !command.displayName().isBlank()) {
            claims.put("name", command.displayName());
        }

        Key key =
                Keys.hmacShaKeyFor(
                        liveKitProperties.getApiSecret().getBytes(StandardCharsets.UTF_8));

        try {
            String token =
                    Jwts.builder()
                            .setIssuer(liveKitProperties.getApiKey())
                            .setSubject(command.identity())
                            .setIssuedAt(Date.from(now))
                            .setNotBefore(Date.from(now))
                            .setExpiration(Date.from(expiresAt))
                            .addClaims(claims)
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();
            log.info(
                    "livekit jwt signing success: roomName={}, identity={}, expiresAt={}",
                    command.roomName(),
                    command.identity(),
                    expiresAt);

            return IssuedLiveKitToken.of(token, expiresAt);
        } catch (Exception e) {
            log.error(
                    "livekit jwt signing failed: roomName={}, identity={}, reason={}",
                    command.roomName(),
                    command.identity(),
                    e.getMessage(),
                    e);
            throw new BaseException(ErrorCode.VIDEO_TOKEN_ISSUE_FAILED);
        }
    }

    private void validate(LiveKitTokenCommand command) {
        if (command == null
                || command.roomName() == null
                || command.roomName().isBlank()
                || command.identity() == null
                || command.identity().isBlank()
                || command.ttlSeconds() <= 0) {
            throw new BaseException(ErrorCode.VIDEO_TOKEN_ISSUE_FAILED);
        }
    }

    private void validateProperties() {
        if (liveKitProperties.getApiKey() == null
                || liveKitProperties.getApiKey().isBlank()
                || liveKitProperties.getApiSecret() == null
                || liveKitProperties.getApiSecret().isBlank()) {
            log.error(
                    "livekit properties invalid: apiKeyPresent={}, apiSecretPresent={}",
                    liveKitProperties.getApiKey() != null
                            && !liveKitProperties.getApiKey().isBlank(),
                    liveKitProperties.getApiSecret() != null
                            && !liveKitProperties.getApiSecret().isBlank());
            throw new BaseException(ErrorCode.VIDEO_TOKEN_ISSUE_FAILED);
        }
    }
}
