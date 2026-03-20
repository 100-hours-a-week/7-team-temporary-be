package molip.server.chat.redis.presence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
public class RedisVideoParticipantPresenceStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${webrtc.presence-ttl-seconds:45}")
    private long presenceTtlSeconds;

    public void upsertOnline(
            Long roomId,
            Long participantId,
            Long userId,
            String nickname,
            String sessionId,
            Boolean cameraEnabled,
            OffsetDateTime now) {
        String member = member(participantId, sessionId);
        String key = presenceKey(roomId, member);

        VideoParticipantPresenceState state =
                VideoParticipantPresenceState.of(
                        roomId,
                        participantId,
                        userId,
                        nickname,
                        sessionId,
                        cameraEnabled,
                        now,
                        now);
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

        VideoParticipantPresenceState current = parse(raw);
        VideoParticipantPresenceState touched = current.withHeartbeatAt(now);
        setPresence(key, touched);
        redisTemplate.expire(key, Duration.ofSeconds(resolveTtlSeconds()));
    }

    public void removeOffline(Long roomId, Long participantId, String sessionId) {
        String member = member(participantId, sessionId);
        redisTemplate.delete(presenceKey(roomId, member));
        redisTemplate.opsForSet().remove(roomMembersKey(roomId), member);
    }

    public List<VideoParticipantPresenceState> removeAllByParticipant(
            Long roomId, Long participantId) {
        Set<String> members = redisTemplate.opsForSet().members(roomMembersKey(roomId));
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<VideoParticipantPresenceState> removed = new ArrayList<>();
        String prefix = participantId + "|";

        for (String member : members) {
            if (member == null || !member.startsWith(prefix)) {
                continue;
            }

            String key = presenceKey(roomId, member);
            String raw = redisTemplate.opsForValue().get(key);
            Boolean deleted = redisTemplate.delete(key);
            redisTemplate.opsForSet().remove(roomMembersKey(roomId), member);

            if ((deleted == null || !deleted) || raw == null || raw.isBlank()) {
                continue;
            }

            removed.add(parse(raw));
        }

        return removed;
    }

    public List<VideoParticipantPresenceState> findOnlineByRoomId(Long roomId) {
        Set<String> members = redisTemplate.opsForSet().members(roomMembersKey(roomId));
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<VideoParticipantPresenceState> results = new ArrayList<>();
        for (String member : members) {
            String key = presenceKey(roomId, member);
            String raw = redisTemplate.opsForValue().get(key);

            if (raw == null || raw.isBlank()) {
                redisTemplate.opsForSet().remove(roomMembersKey(roomId), member);
                continue;
            }

            results.add(parse(raw));
        }

        return results.stream()
                .sorted(
                        Comparator.comparing(VideoParticipantPresenceState::lastHeartbeatAt)
                                .reversed())
                .toList();
    }

    public void updateCameraEnabledByParticipant(
            Long roomId, Long participantId, Boolean cameraEnabled) {
        if (roomId == null || participantId == null || cameraEnabled == null) {
            return;
        }

        Set<String> members = redisTemplate.opsForSet().members(roomMembersKey(roomId));
        if (members == null || members.isEmpty()) {
            return;
        }

        String prefix = participantId + "|";
        for (String member : members) {
            if (member == null || !member.startsWith(prefix)) {
                continue;
            }

            String key = presenceKey(roomId, member);
            String raw = redisTemplate.opsForValue().get(key);
            if (raw == null || raw.isBlank()) {
                redisTemplate.opsForSet().remove(roomMembersKey(roomId), member);
                continue;
            }

            VideoParticipantPresenceState current = parse(raw);
            VideoParticipantPresenceState updated =
                    VideoParticipantPresenceState.of(
                            current.roomId(),
                            current.participantId(),
                            current.userId(),
                            current.nickname(),
                            current.sessionId(),
                            cameraEnabled,
                            current.onlineAt(),
                            current.lastHeartbeatAt());

            setPresence(key, updated);
            redisTemplate.expire(key, Duration.ofSeconds(resolveTtlSeconds()));
        }
    }

    private void setPresence(String key, VideoParticipantPresenceState state) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(state));
        } catch (JsonProcessingException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private VideoParticipantPresenceState parse(String raw) {
        try {
            return objectMapper.readValue(raw, VideoParticipantPresenceState.class);
        } catch (JsonProcessingException exception) {
            log.warn("invalid video presence payload. raw={}", raw);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private long resolveTtlSeconds() {
        return presenceTtlSeconds > 0 ? presenceTtlSeconds : 45L;
    }

    private String roomMembersKey(Long roomId) {
        return "video:presence:room:" + roomId + ":members";
    }

    private String presenceKey(Long roomId, String member) {
        return "video:presence:room:" + roomId + ":member:" + member;
    }

    private String member(Long participantId, String sessionId) {
        return participantId + "|" + sessionId;
    }
}
