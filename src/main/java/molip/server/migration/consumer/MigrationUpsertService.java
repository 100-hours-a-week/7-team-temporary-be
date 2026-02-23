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

        long eventVersion = message.eventVersion();

        switch (aggregateType) {
            case USER -> upsertUser(payload, eventVersion);
            case USER_IMAGE -> upsertUserImage(payload, eventVersion);
            case IMAGE -> upsertImage(payload, eventVersion);
            case DAY_PLAN -> upsertDayPlan(payload, eventVersion);
            case SCHEDULE -> upsertSchedule(payload, eventVersion);
            case SCHEDULE_HISTORY -> upsertScheduleHistory(payload, eventVersion);
            case REFLECTION -> upsertReflection(payload, eventVersion);
            case REFLECTION_IMAGE -> upsertReflectionImage(payload, eventVersion);
            case NOTIFICATION -> upsertNotification(payload, eventVersion);
            case USER_FCM_TOKEN -> upsertUserFcmToken(payload, eventVersion);
            case TERMS_SIGN -> upsertTermsSign(payload, eventVersion);
            case ISSUE -> upsertIssue(payload, eventVersion);
        }
    }

    private void upsertUser(JsonNode payload, long eventVersion) {
        String sql =
                "insert into users (id, email, password, nickname, gender, birth, focus_time_zone, day_end_time, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "email = if(values(version) > version, values(email), email), "
                        + "password = if(values(version) > version, values(password), password), "
                        + "nickname = if(values(version) > version, values(nickname), nickname), "
                        + "gender = if(values(version) > version, values(gender), gender), "
                        + "birth = if(values(version) > version, values(birth), birth), "
                        + "focus_time_zone = if(values(version) > version, values(focus_time_zone), focus_time_zone), "
                        + "day_end_time = if(values(version) > version, values(day_end_time), day_end_time), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
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
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertImage(JsonNode payload, long eventVersion) {
        String sql =
                "insert into image (id, image_type, upload_key, upload_status, expires_at, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "image_type = if(values(version) > version, values(image_type), image_type), "
                        + "upload_key = if(values(version) > version, values(upload_key), upload_key), "
                        + "upload_status = if(values(version) > version, values(upload_status), upload_status), "
                        + "expires_at = if(values(version) > version, values(expires_at), expires_at), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getText(payload, "image_type"),
                getText(payload, "upload_key"),
                getText(payload, "upload_status"),
                getOffsetTimestamp(payload, "expires_at"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertUserImage(JsonNode payload, long eventVersion) {
        String sql =
                "insert into user_image (id, user_id, image_id, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "image_id = if(values(version) > version, values(image_id), image_id), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "image_id"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertDayPlan(JsonNode payload, long eventVersion) {
        String sql =
                "insert into day_plan (id, user_id, plan_date, ai_usage_remaining_count, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "plan_date = if(values(version) > version, values(plan_date), plan_date), "
                        + "ai_usage_remaining_count = if(values(version) > version, values(ai_usage_remaining_count), ai_usage_remaining_count), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getDate(payload, "plan_date"),
                getInteger(payload, "ai_usage_remaining_count"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertSchedule(JsonNode payload, long eventVersion) {
        String sql =
                "insert into schedule (id, day_plan_id, parent_schedule_id, title, status, type, assigned_by, assignment_status, start_at, end_at, "
                        + "estimated_time_range, focus_level, is_urgent, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "day_plan_id = if(values(version) > version, values(day_plan_id), day_plan_id), "
                        + "parent_schedule_id = if(values(version) > version, values(parent_schedule_id), parent_schedule_id), "
                        + "title = if(values(version) > version, values(title), title), "
                        + "status = if(values(version) > version, values(status), status), "
                        + "type = if(values(version) > version, values(type), type), "
                        + "assigned_by = if(values(version) > version, values(assigned_by), assigned_by), "
                        + "assignment_status = if(values(version) > version, values(assignment_status), assignment_status), "
                        + "start_at = if(values(version) > version, values(start_at), start_at), "
                        + "end_at = if(values(version) > version, values(end_at), end_at), "
                        + "estimated_time_range = if(values(version) > version, values(estimated_time_range), estimated_time_range), "
                        + "focus_level = if(values(version) > version, values(focus_level), focus_level), "
                        + "is_urgent = if(values(version) > version, values(is_urgent), is_urgent), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
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
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertScheduleHistory(JsonNode payload, long eventVersion) {
        String sql =
                "insert into schedule_history (id, schedule_id, event_type, prev_start_at, prev_end_at, next_start_at, next_end_at, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "schedule_id = if(values(version) > version, values(schedule_id), schedule_id), "
                        + "event_type = if(values(version) > version, values(event_type), event_type), "
                        + "prev_start_at = if(values(version) > version, values(prev_start_at), prev_start_at), "
                        + "prev_end_at = if(values(version) > version, values(prev_end_at), prev_end_at), "
                        + "next_start_at = if(values(version) > version, values(next_start_at), next_start_at), "
                        + "next_end_at = if(values(version) > version, values(next_end_at), next_end_at), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "schedule_id"),
                getText(payload, "event_type"),
                getTimestamp(payload, "prev_start_at"),
                getTimestamp(payload, "prev_end_at"),
                getTimestamp(payload, "next_start_at"),
                getTimestamp(payload, "next_end_at"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertReflection(JsonNode payload, long eventVersion) {
        String sql =
                "insert into day_reflection (id, user_id, day_plan_id, title, content, is_open, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "day_plan_id = if(values(version) > version, values(day_plan_id), day_plan_id), "
                        + "title = if(values(version) > version, values(title), title), "
                        + "content = if(values(version) > version, values(content), content), "
                        + "is_open = if(values(version) > version, values(is_open), is_open), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "day_plan_id"),
                getText(payload, "title"),
                getText(payload, "content"),
                getBoolean(payload, "is_open"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertReflectionImage(JsonNode payload, long eventVersion) {
        String sql =
                "insert into day_reflection_image (id, day_reflection_id, image_id, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "day_reflection_id = if(values(version) > version, values(day_reflection_id), day_reflection_id), "
                        + "image_id = if(values(version) > version, values(image_id), image_id), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "day_reflection_id"),
                getLong(payload, "image_id"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertNotification(JsonNode payload, long eventVersion) {
        String sql =
                "insert into notification (id, user_id, schedule_id, type, title, content, status, scheduled_at, sent_at, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "schedule_id = if(values(version) > version, values(schedule_id), schedule_id), "
                        + "type = if(values(version) > version, values(type), type), "
                        + "title = if(values(version) > version, values(title), title), "
                        + "content = if(values(version) > version, values(content), content), "
                        + "status = if(values(version) > version, values(status), status), "
                        + "scheduled_at = if(values(version) > version, values(scheduled_at), scheduled_at), "
                        + "sent_at = if(values(version) > version, values(sent_at), sent_at), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
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
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertUserFcmToken(JsonNode payload, long eventVersion) {
        String sql =
                "insert into user_fcm_token (id, user_id, fcm_token, platform, is_active, last_seen_at, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "fcm_token = if(values(version) > version, values(fcm_token), fcm_token), "
                        + "platform = if(values(version) > version, values(platform), platform), "
                        + "is_active = if(values(version) > version, values(is_active), is_active), "
                        + "last_seen_at = if(values(version) > version, values(last_seen_at), last_seen_at), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getText(payload, "fcm_token"),
                getText(payload, "platform"),
                getBoolean(payload, "is_active"),
                getTimestamp(payload, "last_seen_at"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertTermsSign(JsonNode payload, long eventVersion) {
        String sql =
                "insert into terms_sign (id, user_id, terms_id, is_agreed, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "terms_id = if(values(version) > version, values(terms_id), terms_id), "
                        + "is_agreed = if(values(version) > version, values(is_agreed), is_agreed), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getLong(payload, "terms_id"),
                getBoolean(payload, "is_agreed"),
                eventVersion,
                getTimestamp(payload, "created_at"),
                getTimestamp(payload, "updated_at"),
                getTimestamp(payload, "deleted_at"));
    }

    private void upsertIssue(JsonNode payload, long eventVersion) {
        String sql =
                "insert into issue (id, user_id, content, version, created_at, updated_at, deleted_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "user_id = if(values(version) > version, values(user_id), user_id), "
                        + "content = if(values(version) > version, values(content), content), "
                        + "updated_at = if(values(version) > version, values(updated_at), updated_at), "
                        + "deleted_at = if(values(version) > version, values(deleted_at), deleted_at), "
                        + "version = if(values(version) > version, values(version), version)";
        migrationJdbcTemplate.update(
                sql,
                getLong(payload, "id"),
                getLong(payload, "user_id"),
                getText(payload, "content"),
                eventVersion,
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
