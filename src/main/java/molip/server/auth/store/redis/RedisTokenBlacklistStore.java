package molip.server.auth.store.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import molip.server.auth.store.TokenBlacklistStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistStore implements TokenBlacklistStore {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void add(Long userId, String token) {
        redisTemplate.opsForValue().set(userKey(userId), token);
        redisTemplate.opsForValue().set(tokenKey(token), String.valueOf(userId));
    }

    @Override
    public boolean contains(long userId, String token) {
        String blackListedToken = redisTemplate.opsForValue().get(userKey(userId));
        return blackListedToken != null && blackListedToken.equals(token);
    }

    @Override
    public boolean contains(String token) {
        return redisTemplate.hasKey(tokenKey(token));
    }

    private String userKey(Long userId) {
        return "auth:blacklist:user:" + userId;
    }

    private String tokenKey(String token) {
        return "auth:blacklist:token:" + hash(token);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hashed) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
