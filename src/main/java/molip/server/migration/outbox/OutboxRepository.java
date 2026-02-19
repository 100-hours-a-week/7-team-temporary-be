package molip.server.migration.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.migration.event.DomainEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxRepository {

    private static final String TABLE = "outbox_event";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void save(DomainEvent event) {
        String payload = serializePayload(event);
        String sql =
                "insert into "
                        + TABLE
                        + " (event_id, aggregate_type, aggregate_id, event_type, occurred_at, payload, status, retry_count, last_error, created_at, updated_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbcTemplate.update(
                sql,
                event.eventId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType().name(),
                Timestamp.from(event.occurredAt().toInstant()),
                payload,
                OutboxStatus.PENDING.name(),
                0,
                null,
                Timestamp.from(now.toInstant()),
                Timestamp.from(now.toInstant()));
    }

    public List<OutboxRecord> findPending(int limit) {
        String sql =
                "select id, event_id, aggregate_type, aggregate_id, event_type, occurred_at, payload, status, retry_count, last_error, created_at, updated_at "
                        + "from "
                        + TABLE
                        + " where status = ? order by id asc limit ?";
        return jdbcTemplate.query(sql, outboxRowMapper(), OutboxStatus.PENDING.name(), limit);
    }

    public List<OutboxRecord> findRetryableFailed(
            int limit, int maxRetryCount, OffsetDateTime retryBefore) {
        String sql =
                "select id, event_id, aggregate_type, aggregate_id, event_type, occurred_at, payload, status, retry_count, last_error, created_at, updated_at "
                        + "from "
                        + TABLE
                        + " where status = ? and retry_count < ? and updated_at <= ? "
                        + "order by id asc limit ?";
        return jdbcTemplate.query(
                sql,
                outboxRowMapper(),
                OutboxStatus.FAILED.name(),
                maxRetryCount,
                Timestamp.from(retryBefore.toInstant()),
                limit);
    }

    public List<OutboxRecord> findDlqCandidates(int limit, int maxRetryCount) {
        String sql =
                "select id, event_id, aggregate_type, aggregate_id, event_type, occurred_at, payload, status, retry_count, last_error, created_at, updated_at "
                        + "from "
                        + TABLE
                        + " where status = ? and retry_count >= ? order by id asc limit ?";
        return jdbcTemplate.query(
                sql, outboxRowMapper(), OutboxStatus.FAILED.name(), maxRetryCount, limit);
    }

    public void markSent(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String inClause = ids.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        String sql =
                "update "
                        + TABLE
                        + " set status = ?, updated_at = ? where id in ("
                        + inClause
                        + ")";
        Object[] params = new Object[ids.size() + 2];
        params[0] = OutboxStatus.SENT.name();
        params[1] = Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant());
        for (int i = 0; i < ids.size(); i++) {
            params[i + 2] = ids.get(i);
        }
        jdbcTemplate.update(sql, params);
    }

    public void markPending(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String inClause = ids.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        String sql =
                "update "
                        + TABLE
                        + " set status = ?, updated_at = ? where id in ("
                        + inClause
                        + ")";
        Object[] params = new Object[ids.size() + 2];
        params[0] = OutboxStatus.PENDING.name();
        params[1] = Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant());
        for (int i = 0; i < ids.size(); i++) {
            params[i + 2] = ids.get(i);
        }
        jdbcTemplate.update(sql, params);
    }

    public void markDlq(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String inClause = ids.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        String sql =
                "update "
                        + TABLE
                        + " set status = ?, updated_at = ? where id in ("
                        + inClause
                        + ")";
        Object[] params = new Object[ids.size() + 2];
        params[0] = OutboxStatus.DLQ.name();
        params[1] = Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant());
        for (int i = 0; i < ids.size(); i++) {
            params[i + 2] = ids.get(i);
        }
        jdbcTemplate.update(sql, params);
    }

    public void markFailed(Long id, String lastError) {
        String sql =
                "update "
                        + TABLE
                        + " set status = ?, retry_count = retry_count + 1, last_error = ?, updated_at = ? "
                        + "where id = ?";
        jdbcTemplate.update(
                sql,
                OutboxStatus.FAILED.name(),
                lastError,
                Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()),
                id);
    }

    private String serializePayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event.payload());
        } catch (JsonProcessingException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private RowMapper<OutboxRecord> outboxRowMapper() {
        return new RowMapper<>() {
            @Override
            public OutboxRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new OutboxRecord(
                        rs.getLong("id"),
                        rs.getString("event_id"),
                        rs.getString("aggregate_type"),
                        rs.getString("aggregate_id"),
                        rs.getString("event_type"),
                        rs.getObject("occurred_at", OffsetDateTime.class),
                        rs.getString("payload"),
                        OutboxStatus.valueOf(rs.getString("status")),
                        rs.getInt("retry_count"),
                        rs.getString("last_error"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class));
            }
        };
    }
}
