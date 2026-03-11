package molip.server.dev;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.config.LiveKitProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveKitProbeService {

    private final LiveKitProperties liveKitProperties;

    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("ok", false);

        String url = liveKitProperties.getUrl();
        String apiKey = liveKitProperties.getApiKey();
        String apiSecret = liveKitProperties.getApiSecret();

        if (isBlank(url) || isBlank(apiKey) || isBlank(apiSecret)) {
            result.put("message", "LIVEKIT_URL/API_KEY/API_SECRET 중 누락된 값이 있습니다.");
            result.put("url", url);
            return result;
        }

        String apiBase = toApiBaseUrl(url);
        String endpoint = apiBase + "/twirp/livekit.RoomService/ListRooms";
        String serverToken = createServerToken(apiKey, apiSecret);

        try {
            String body =
                    RestClient.create()
                            .post()
                            .uri(URI.create(endpoint))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + serverToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{}")
                            .retrieve()
                            .body(String.class);

            result.put("ok", true);
            result.put("message", "LiveKit API 연결 성공");
            result.put("endpoint", endpoint);
            result.put("response", body);
            return result;
        } catch (Exception exception) {
            log.error(
                    "livekit ping failed: endpoint={}, reason={}",
                    endpoint,
                    exception.getMessage());
            result.put("message", "LiveKit API 연결 실패");
            result.put("endpoint", endpoint);
            result.put("error", exception.getMessage());
            return result;
        }
    }

    private String createServerToken(String apiKey, String apiSecret) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60L);

        Map<String, Object> videoGrant = new HashMap<>();
        videoGrant.put("roomList", true);
        videoGrant.put("roomAdmin", true);
        videoGrant.put("roomCreate", true);

        Map<String, Object> claims = new HashMap<>();
        claims.put("video", videoGrant);

        Key key = Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setIssuer(apiKey)
                .setSubject("dev-livekit-probe")
                .setIssuedAt(java.util.Date.from(now))
                .setNotBefore(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(expiresAt))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String toApiBaseUrl(String liveKitUrl) {
        if (liveKitUrl.startsWith("wss://")) {
            return "https://" + liveKitUrl.substring("wss://".length());
        }
        if (liveKitUrl.startsWith("ws://")) {
            return "http://" + liveKitUrl.substring("ws://".length());
        }
        return liveKitUrl;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
