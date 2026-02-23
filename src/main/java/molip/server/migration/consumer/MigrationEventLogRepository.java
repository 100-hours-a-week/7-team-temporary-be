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
public class MigrationEventLogRepository {

    private static final String TABLE = "migration_event_log";

    @Qualifier("migrationJdbcTemplate")
    private final JdbcTemplate migrationJdbcTemplate;

    public void save(
            String eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            long eventVersion,
            OffsetDateTime occurredAt,
            String payloadJson) {
        String sql =
                "insert into "
                        + TABLE
                        + " (event_id, aggregate_type, aggregate_id, event_type, event_version, occurred_at, payload, created_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?)";
        migrationJdbcTemplate.update(
                sql,
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                eventVersion,
                Timestamp.from(occurredAt.toInstant()),
                payloadJson,
                Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
    }
}
