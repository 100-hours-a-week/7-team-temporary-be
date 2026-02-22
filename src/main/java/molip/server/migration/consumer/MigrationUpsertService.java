package molip.server.migration.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import molip.server.migration.event.AggregateType;
import molip.server.migration.outbox.OutboxMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = {"migration.enabled", "migration.datasource.url"},
        havingValue = "true")
public class MigrationUpsertService {

    @Qualifier("migrationJdbcTemplate")
    private final JdbcTemplate migrationJdbcTemplate;

    public void upsert(OutboxMessage message) {
        if (message == null || message.payload() == null) {
            return;
        }
        AggregateType aggregateType = AggregateType.valueOf(message.aggregateType());
        JsonNode payload = message.payload();

        switch (aggregateType) {
            case USER -> upsertUser(payload);
            case USER_IMAGE -> upsertUserImage(payload);
            case IMAGE -> upsertImage(payload);
            case DAY_PLAN -> upsertDayPlan(payload);
            case SCHEDULE -> upsertSchedule(payload);
            case SCHEDULE_HISTORY -> upsertScheduleHistory(payload);
            case REFLECTION -> upsertReflection(payload);
            case REFLECTION_IMAGE -> upsertReflectionImage(payload);
            case NOTIFICATION -> upsertNotification(payload);
            case USER_FCM_TOKEN -> upsertUserFcmToken(payload);
            case TERMS_SIGN -> upsertTermsSign(payload);
            case ISSUE -> upsertIssue(payload);
        }
    }

    private void upsertUser(JsonNode payload) {
        String sql =
                "insert into users (id, email, password, nickname, gender, birth, focus_time_zone, day_end_time, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "email = values(email), password = values(password), nickname = values(nickname), gender = values(gender), "
                        + "birth = values(birth), focus_time_zone = values(focus_time_zone), day_end_time = values(day_end_time), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getText(payload, "email"),
                getText(payload, "password"),
                getText(payload, "nickname"),
                getText(payload, "gender"),
                getDate(payload, "birth"),
                getText(payload, "focus_time_zone"),
                getTime(payload, "day_end_time"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertImage(JsonNode payload) {
        String sql =
                "insert into image (id, image_type, upload_key, upload_status, expires_at, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "image_type = values(image_type), upload_key = values(upload_key), upload_status = values(upload_status), "
                        + "expires_at = values(expires_at), updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getText(payload, "image_type"),
                getText(payload, "upload_key"),
                getText(payload, "upload_status"),
                getOffsetTimestamp(payload, "expires_at"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertUserImage(JsonNode payload) {
        String sql =
                "insert into user_image (id, user_id, image_id, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), image_id = values(image_id), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "image_id"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertDayPlan(JsonNode payload) {
        String sql =
                "insert into day_plan (id, user_id, plan_date, ai_usage_remaining_count, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), plan_date = values(plan_date), ai_usage_remaining_count = values(ai_usage_remaining_count), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getDate(payload, "plan_date"),
                getInteger(payload, "ai_usage_remaining_count"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertSchedule(JsonNode payload) {
        String sql =
                "insert into schedule (id, day_plan_id, parent_schedule_id, title, status, type, assigned_by, assignment_status, start_at, end_at, "
                        + "estimated_time_range, focus_level, is_urgent, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "day_plan_id = values(day_plan_id), parent_schedule_id = values(parent_schedule_id), title = values(title), "
                        + "status = values(status), type = values(type), assigned_by = values(assigned_by), assignment_status = values(assignment_status), "
                        + "start_at = values(start_at), end_at = values(end_at), estimated_time_range = values(estimated_time_range), "
                        + "focus_level = values(focus_level), is_urgent = values(is_urgent), updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "day_plan_id"),
                getLong(payload, "parent_schedule_id"),
                getText(payload, "title"),
                getText(payload, "status"),
                getText(payload, "type"),
                getText(payload, "assigned_by"),
                getText(payload, "assignment_status"),
                getTime(payload, "start_at"),
                getTime(payload, "end_at"),
                getText(payload, "estimated_time_range"),
                getInteger(payload, "focus_level"),
                getBoolean(payload, "is_urgent"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertScheduleHistory(JsonNode payload) {
        String sql =
                "insert into schedule_history (id, schedule_id, event_type, prev_start_at, prev_end_at, next_start_at, next_end_at, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "schedule_id = values(schedule_id), event_type = values(event_type), prev_start_at = values(prev_start_at), "
                        + "prev_end_at = values(prev_end_at), next_start_at = values(next_start_at), next_end_at = values(next_end_at), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "schedule_id"),
                getText(payload, "event_type"),
                getTimestamp(payload, "prev_start_at"),
                getTimestamp(payload, "prev_end_at"),
                getTimestamp(payload, "next_start_at"),
                getTimestamp(payload, "next_end_at"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertReflection(JsonNode payload) {
        String sql =
                "insert into day_reflection (id, user_id, day_plan_id, title, content, is_open, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), day_plan_id = values(day_plan_id), title = values(title), content = values(content), "
                        + "is_open = values(is_open), updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "day_plan_id"),
                getText(payload, "title"),
                getText(payload, "content"),
                getBoolean(payload, "is_open"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertReflectionImage(JsonNode payload) {
        String sql =
                "insert into day_reflection_image (id, day_reflection_id, image_id, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "day_reflection_id = values(day_reflection_id), image_id = values(image_id), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "day_reflection_id"),
                getLong(payload, "image_id"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertNotification(JsonNode payload) {
        String sql =
                "insert into notification (id, user_id, schedule_id, type, title, content, status, scheduled_at, sent_at, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), schedule_id = values(schedule_id), type = values(type), title = values(title), "
                        + "content = values(content), status = values(status), scheduled_at = values(scheduled_at), sent_at = values(sent_at), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "schedule_id"),
                getText(payload, "type"),
                getText(payload, "title"),
                getText(payload, "content"),
                getText(payload, "status"),
                getTimestamp(payload, "scheduled_at"),
                getTimestamp(payload, "sent_at"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertUserFcmToken(JsonNode payload) {
        String sql =
                "insert into user_fcm_token (id, user_id, fcm_token, platform, is_active, last_seen_at, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), fcm_token = values(fcm_token), platform = values(platform), is_active = values(is_active), "
                        + "last_seen_at = values(last_seen_at), updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getText(payload, "fcm_token"),
                getText(payload, "platform"),
                getBoolean(payload, "is_active"),
                getTimestamp(payload, "last_seen_at"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertTermsSign(JsonNode payload) {
        String sql =
                "insert into terms_sign (id, user_id, terms_id, is_agreed, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), terms_id = values(terms_id), is_agreed = values(is_agreed), "
                        + "updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "terms_id"),
                getBoolean(payload, "is_agreed"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertIssue(JsonNode payload) {
        String sql =
                "insert into issue (id, user_id, content, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = values(user_id), content = values(content), updated_at = values(updated_at), deleted_at = values(deleted_at)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getText(payload, "content"),
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private String getText(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private Long getLong(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.longValue();
        }
        String value = node.asText();
        return value.isBlank() ? null : Long.valueOf(value);
    }

    private Integer getInteger(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.intValue();
        }
        String value = node.asText();
        return value.isBlank() ? null : Integer.valueOf(value);
    }

    private Boolean getBoolean(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        String value = node.asText();
        return value.isBlank() ? null : Boolean.valueOf(value);
    }

    private Timestamp getTimestamp(JsonNode payload, String field) {
        String value = getText(payload, field);
        if (value == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.parse(value);
        return Timestamp.valueOf(dateTime);
    }

    private Timestamp getOffsetTimestamp(JsonNode payload, String field) {
        String value = getText(payload, field);
        if (value == null) {
            return null;
        }
        OffsetDateTime dateTime = OffsetDateTime.parse(value);
        return Timestamp.from(dateTime.toInstant());
    }

    private Date getDate(JsonNode payload, String field) {
        String value = getText(payload, field);
        if (value == null) {
            return null;
        }
        LocalDate date = LocalDate.parse(value);
        return Date.valueOf(date);
    }

    private Time getTime(JsonNode payload, String field) {
        String value = getText(payload, field);
        if (value == null) {
            return null;
        }
        LocalTime time = LocalTime.parse(value);
        return Time.valueOf(time);
    }
}
