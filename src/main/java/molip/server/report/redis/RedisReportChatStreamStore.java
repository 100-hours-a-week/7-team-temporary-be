package molip.server.report.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisReportChatStreamStore {

    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void initialize(Long reportId, Long inputMessageId, Long streamMessageId) {
        write(
                reportId,
                streamMessageId,
                ReportChatStreamState.initial(inputMessageId, streamMessageId));
    }

    public Optional<ReportChatStreamState> find(Long reportId, Long streamMessageId) {
        String raw = redisTemplate.opsForValue().get(key(reportId, streamMessageId));

        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(raw, ReportChatStreamState.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public void appendChunk(Long reportId, Long streamMessageId, Long sequence, String delta) {
        ReportChatStreamState current =
                find(reportId, streamMessageId)
                        .orElseGet(() -> ReportChatStreamState.initial(null, streamMessageId));

        if (sequence != null
                && sequence <= (current.lastSequence() == null ? 0L : current.lastSequence())) {
            return;
        }

        write(
                reportId,
                streamMessageId,
                current.withChunk(sequence == null ? 0L : sequence, delta));
    }

    public void updateStatus(Long reportId, Long streamMessageId, String status) {
        ReportChatStreamState current =
                find(reportId, streamMessageId)
                        .orElseGet(() -> ReportChatStreamState.initial(null, streamMessageId));

        write(reportId, streamMessageId, current.withStatus(status));
    }

    private void write(Long reportId, Long streamMessageId, ReportChatStreamState state) {
        try {
            String raw = objectMapper.writeValueAsString(state);

            redisTemplate.opsForValue().set(key(reportId, streamMessageId), raw, TTL);
        } catch (JsonProcessingException ignored) {
        }
    }

    private String key(Long reportId, Long streamMessageId) {
        return "report:chat:stream:" + reportId + ":" + streamMessageId;
    }
}
