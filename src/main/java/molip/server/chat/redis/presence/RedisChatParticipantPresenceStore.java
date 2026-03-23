package molip.server.chat.redis.presence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatParticipantPresenceStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chat.presence-ttl-seconds:45}")
    private long presenceTtlSeconds;

    public void upsertOnline(
            Long roomId, Long participantId, Long userId, String sessionId, OffsetDateTime now) {
        String member = member(participantId, sessionId);
        String key = presenceKey(roomId, member);

        ChatParticipantPresenceState state =
                ChatParticipantPresenceState.of(roomId, participantId, userId, sessionId, now, now);
        setPresence(key, state);
        redisTemplate.opsForSet().add(roomMembersKey(roomId), member);
        redisTemplate.expire(key, Duration.ofSeconds(resolveTtlSeconds()));
    }

    public void touchHeartbeat(
            Long roomId, Long participantId, String sessionId, OffsetDateTime now) {
        String member = member(participantId, sessionId);
        String key = presenceKey(roomId, member);
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null || raw.isBlank()) {
            return;
        }

        ChatParticipantPresenceState touched = parse(raw).withHeartbeatAt(now);
        setPresence(key, touched);
        redisTemplate.expire(key, Duration.ofSeconds(resolveTtlSeconds()));
    }

    public void removeOffline(Long roomId, Long participantId, String sessionId) {
        String member = member(participantId, sessionId);
        redisTemplate.delete(presenceKey(roomId, member));
        redisTemplate.opsForSet().remove(roomMembersKey(roomId), member);
    }

    public boolean isOnline(Long roomId, Long participantId) {
        Set<String> members = redisTemplate.opsForSet().members(roomMembersKey(roomId));
        if (members == null || members.isEmpty()) {
            return false;
        }

        String prefix = participantId + "|";
        for (String member : members) {
            if (member == null || !member.startsWith(prefix)) {
                continue;
            }

            String raw = redisTemplate.opsForValue().get(presenceKey(roomId, member));
            if (raw != null && !raw.isBlank()) {
                return true;
            }

            redisTemplate.opsForSet().remove(roomMembersKey(roomId), member);
        }

        return false;
    }

    private void setPresence(String key, ChatParticipantPresenceState state) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(state));
        } catch (JsonProcessingException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ChatParticipantPresenceState parse(String raw) {
        try {
            return objectMapper.readValue(raw, ChatParticipantPresenceState.class);
        } catch (JsonProcessingException exception) {
            log.warn("invalid chat presence payload. raw={}", raw);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private long resolveTtlSeconds() {
        return presenceTtlSeconds > 0 ? presenceTtlSeconds : 45L;
    }

    private String roomMembersKey(Long roomId) {
        return "chat:presence:room:" + roomId + ":members";
    }

    private String presenceKey(Long roomId, String member) {
        return "chat:presence:room:" + roomId + ":member:" + member;
    }

    private String member(Long participantId, String sessionId) {
        return participantId + "|" + sessionId;
    }
}
