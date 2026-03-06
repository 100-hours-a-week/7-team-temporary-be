package molip.server.migration.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import molip.server.image.entity.Image;
import molip.server.issue.entity.Issue;
import molip.server.notification.entity.Notification;
import molip.server.notification.entity.UserFcmToken;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.DayReflectionImage;
import molip.server.reflection.entity.ReflectionLike;
import molip.server.schedule.entity.DayPlan;
import molip.server.schedule.entity.Schedule;
import molip.server.schedule.entity.ScheduleActionLog;
import molip.server.schedule.entity.ScheduleHistory;
import molip.server.terms.entity.TermsSign;
import molip.server.user.entity.UserImage;
import molip.server.user.entity.Users;

public final class OutboxPayloadMapper {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter OFFSET_DATETIME_FORMAT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private OutboxPayloadMapper() {}

    public static Map<String, Object> user(Users user) {
        Map<String, Object> payload = baseEntityPayload(user.getId(), user.getVersion());
        payload.put("email", user.getEmail());
        payload.put("password", user.getPassword());
        payload.put("nickname", user.getNickname());
        payload.put("gender", user.getGender().name());
        payload.put("birth", formatDate(user.getBirth()));
        payload.put("focus_time_zone", user.getFocusTimeZone().name());
        payload.put("day_end_time", formatTime(user.getDayEndTime()));
        payload.putAll(auditPayload(user.getCreatedAt(), user.getUpdatedAt(), user.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> image(Image image) {
        Map<String, Object> payload = baseEntityPayload(image.getId(), image.getVersion());
        payload.put("image_type", image.getImageType().name());
        payload.put("upload_key", image.getUploadKey());
        payload.put("upload_status", image.getUploadStatus().name());
        payload.put("expires_at", formatOffsetDateTime(image.getExpiresAt()));
        payload.putAll(
                auditPayload(image.getCreatedAt(), image.getUpdatedAt(), image.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> userImage(UserImage userImage) {
        Map<String, Object> payload = baseEntityPayload(userImage.getId(), userImage.getVersion());
        payload.put("user_id", userImage.getUser().getId());
        payload.put("image_id", userImage.getImage().getId());
        payload.putAll(
                auditPayload(
                        userImage.getCreatedAt(),
                        userImage.getUpdatedAt(),
                        userImage.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> dayPlan(DayPlan dayPlan) {
        Map<String, Object> payload = baseEntityPayload(dayPlan.getId(), dayPlan.getVersion());
        payload.put("user_id", dayPlan.getUser().getId());
        payload.put("plan_date", formatDate(dayPlan.getPlanDate()));
        payload.put("ai_usage_remaining_count", dayPlan.getAiUsageRemainingCount());
        payload.putAll(
                auditPayload(
                        dayPlan.getCreatedAt(), dayPlan.getUpdatedAt(), dayPlan.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> schedule(Schedule schedule) {
        Map<String, Object> payload = baseEntityPayload(schedule.getId(), schedule.getVersion());
        payload.put("day_plan_id", schedule.getDayPlan().getId());
        payload.put(
                "parent_schedule_id",
                schedule.getParentSchedule() == null ? null : schedule.getParentSchedule().getId());
        payload.put("title", schedule.getTitle());
        payload.put("status", schedule.getStatus().name());
        payload.put("type", schedule.getType().name());
        payload.put("assigned_by", schedule.getAssignedBy().name());
        payload.put("assignment_status", schedule.getAssignmentStatus().name());
        payload.put("start_at", formatTime(schedule.getStartAt()));
        payload.put("end_at", formatTime(schedule.getEndAt()));
        payload.put(
                "estimated_time_range",
                schedule.getEstimatedTimeRange() == null
                        ? null
                        : schedule.getEstimatedTimeRange().name());
        payload.put("focus_level", schedule.getFocusLevel());
        payload.put("is_urgent", schedule.getIsUrgent());
        payload.putAll(
                auditPayload(
                        schedule.getCreatedAt(), schedule.getUpdatedAt(), schedule.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> scheduleHistory(ScheduleHistory history) {
        Map<String, Object> payload = baseEntityPayload(history.getId(), history.getVersion());
        payload.put("schedule_id", history.getSchedule().getId());
        payload.put("event_type", history.getEventType().name());
        payload.put("prev_start_at", formatDateTime(history.getPrevStartAt()));
        payload.put("prev_end_at", formatDateTime(history.getPrevEndAt()));
        payload.put("next_start_at", formatDateTime(history.getNextStartAt()));
        payload.put("next_end_at", formatDateTime(history.getNextEndAt()));
        payload.putAll(
                auditPayload(
                        history.getCreatedAt(), history.getUpdatedAt(), history.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> scheduleActionLog(ScheduleActionLog actionLog) {
        Map<String, Object> payload = baseEntityPayload(actionLog.getId(), actionLog.getVersion());
        payload.put("user_id", actionLog.getUserId());
        payload.put("schedule_id", actionLog.getScheduleId());
        payload.put("action_type", actionLog.getActionType().name());
        payload.put("api_path", actionLog.getApiPath());
        payload.putAll(
                auditPayload(
                        actionLog.getCreatedAt(),
                        actionLog.getUpdatedAt(),
                        actionLog.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> reflection(DayReflection reflection) {
        Map<String, Object> payload =
                baseEntityPayload(reflection.getId(), reflection.getVersion());
        payload.put("user_id", reflection.getUser().getId());
        payload.put("day_plan_id", reflection.getDayPlan().getId());
        payload.put("title", reflection.getTitle());
        payload.put("content", reflection.getContent());
        payload.put("is_open", reflection.isOpen());
        payload.putAll(
                auditPayload(
                        reflection.getCreatedAt(),
                        reflection.getUpdatedAt(),
                        reflection.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> reflectionImage(DayReflectionImage reflectionImage) {
        Map<String, Object> payload =
                baseEntityPayload(reflectionImage.getId(), reflectionImage.getVersion());
        payload.put("day_reflection_id", reflectionImage.getDayReflection().getId());
        payload.put("image_id", reflectionImage.getImage().getId());
        payload.putAll(
                auditPayload(
                        reflectionImage.getCreatedAt(),
                        reflectionImage.getUpdatedAt(),
                        reflectionImage.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> reflectionLike(ReflectionLike reflectionLike) {
        Map<String, Object> payload =
                baseEntityPayload(reflectionLike.getId(), reflectionLike.getVersion());
        payload.put("user_id", reflectionLike.getUser().getId());
        payload.put("day_reflection_id", reflectionLike.getReflection().getId());
        payload.putAll(
                auditPayload(
                        reflectionLike.getCreatedAt(),
                        reflectionLike.getUpdatedAt(),
                        reflectionLike.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> notification(Notification notification) {
        Map<String, Object> payload =
                baseEntityPayload(notification.getId(), notification.getVersion());
        payload.put("user_id", notification.getUser().getId());
        payload.put("schedule_id", notification.getScheduleId());
        payload.put("type", notification.getType().name());
        payload.put("title", notification.getTitle());
        payload.put("content", notification.getContent());
        payload.put("status", notification.getStatus().name());
        payload.put("scheduled_at", formatDateTime(notification.getScheduledAt()));
        payload.put("sent_at", formatDateTime(notification.getSentAt()));
        payload.putAll(
                auditPayload(
                        notification.getCreatedAt(),
                        notification.getUpdatedAt(),
                        notification.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> userFcmToken(UserFcmToken token) {
        Map<String, Object> payload = baseEntityPayload(token.getId(), token.getVersion());
        payload.put("user_id", token.getUser().getId());
        payload.put("fcm_token", token.getFcmToken());
        payload.put("platform", token.getPlatform().name());
        payload.put("is_active", token.getIsActive());
        payload.put("last_seen_at", formatDateTime(token.getLastSeenAt()));
        payload.putAll(
                auditPayload(token.getCreatedAt(), token.getUpdatedAt(), token.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> termsSign(TermsSign sign) {
        Map<String, Object> payload = baseEntityPayload(sign.getId(), sign.getVersion());
        payload.put("user_id", sign.getUser().getId());
        payload.put("terms_id", sign.getTerms().getId());
        payload.put("is_agreed", sign.isAgreed());
        payload.putAll(auditPayload(sign.getCreatedAt(), sign.getUpdatedAt(), sign.getDeletedAt()));
        return payload;
    }

    public static Map<String, Object> issue(Issue issue) {
        Map<String, Object> payload = baseEntityPayload(issue.getId(), issue.getVersion());
        payload.put("user_id", issue.getUser().getId());
        payload.put("content", issue.getContent());
        payload.putAll(
                auditPayload(issue.getCreatedAt(), issue.getUpdatedAt(), issue.getDeletedAt()));
        return payload;
    }

    private static Map<String, Object> baseEntityPayload(Long id, Long version) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("version", version);
        return payload;
    }

    private static Map<String, Object> auditPayload(
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("created_at", formatDateTime(createdAt));
        payload.put("updated_at", formatDateTime(updatedAt));
        payload.put("deleted_at", formatDateTime(deletedAt));
        return payload;
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    private static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMAT);
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMAT);
    }

    private static String formatOffsetDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(OFFSET_DATETIME_FORMAT);
    }
}
