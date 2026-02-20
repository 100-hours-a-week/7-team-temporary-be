package molip.server.migration.consumer;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"migration.enabled", "migration.datasource.url"},
        havingValue = "true")
public class EventApplyLogRepository {

    private static final String TABLE = "event_apply_log";

    @Qualifier("migrationJdbcTemplate")
    private final JdbcTemplate migrationJdbcTemplate;

    public boolean existsByEventId(String eventId) {
        String sql = "select count(1) from " + TABLE + " where event_id = ?";
        Integer count = migrationJdbcTemplate.queryForObject(sql, Integer.class, eventId);
        return count != null && count > 0;
    }

    public void save(String eventId, String aggregateType, String aggregateId, String eventType) {
        String sql =
                "insert into "
                        + TABLE
                        + " (event_id, aggregate_type, aggregate_id, event_type, applied_at) "
                        + "values (?, ?, ?, ?, ?)";
        migrationJdbcTemplate.update(
                sql,
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
    }
}
