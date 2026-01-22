package molip.server.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(
            Long userId, String role, Long tokenVersion, String deviceId, String jti) {
        return generateToken(
                buildClaims(userId, role, tokenVersion, deviceId, jti), accessTokenExpirationMs);
    }

    public String createRefreshToken(
            Long userId, String role, Long tokenVersion, String deviceId, String jti) {
        return generateToken(
                buildClaims(userId, role, tokenVersion, deviceId, jti), refreshTokenExpirationMs);
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims == null ? null : claims.get("userId", Long.class);
    }

    public Long extractTokenVersion(String token) {
        Claims claims = extractClaims(token);
        return claims == null ? null : claims.get("tv", Long.class);
    }

    public String extractDeviceId(String token) {
        Claims claims = extractClaims(token);
        return claims == null ? null : claims.get("deviceId", String.class);
    }

    public boolean isExpired(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return true;
        }
        Date exp = claims.getExpiration();
        return exp.before(new Date());
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    private String generateToken(Map<String, Object> claims, long expireTimeMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Map<String, Object> buildClaims(
            Long userId, String role, Long tokenVersion, String deviceId, String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("tv", tokenVersion);
        claims.put("deviceId", deviceId);
        claims.put("jti", jti);
        return claims;
    }
}
