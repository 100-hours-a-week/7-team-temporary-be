package molip.server.notification.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.common.enums.NotificationType;
import molip.server.migration.outbox.OutboxEvent;
import molip.server.migration.outbox.OutboxEventJpaRepository;
import molip.server.notification.entity.Notification;
import molip.server.notification.metrics.ChatMessageAlertMetrics;
import molip.server.notification.sender.NotificationSender;
import molip.server.notification.service.NotificationService;
import molip.server.notification.service.UserFcmTokenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchFacade {

    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final NotificationService notificationService;
    private final UserFcmTokenService userFcmTokenService;
    private final NotificationSender notificationSender;
    private final ChatMessageAlertMetrics chatMessageAlertMetrics;
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void dispatchPendingNotifications(int batchSize) {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications =
                notificationService.getPendingNotifications(now, batchSize);

        Map<Long, Map<String, Object>> outboxPayloadByNotificationId =
                loadNotificationOutboxPayloads(notifications);

        for (Notification notification : notifications) {

            Long userId = notification.getUser().getId();
            boolean isChatMessage = notification.getType() == NotificationType.CHAT_MESSAGE;
            if (isChatMessage) {
                chatMessageAlertMetrics.recordDispatchAttempt();
            }

            List<String> tokens = userFcmTokenService.getActiveTokens(userId);

            if (tokens.isEmpty()) {
                notificationService.markFailed(notification);
                continue;
            }

            try {
                Map<String, String> payload =
                        buildFcmPayload(
                                notification,
                                outboxPayloadByNotificationId.get(notification.getId()),
                                now);
                notificationSender.send(
                        notification.getTitle(), notification.getContent(), payload, tokens);

                notificationService.markSent(notification, now);
                if (isChatMessage) {
                    chatMessageAlertMetrics.recordDispatchSent(notification.getScheduledAt(), now);
                }
            } catch (Exception e) {
                log.warn(
                        "notification dispatch failed: notificationId={}, type={}, userId={}, reason={}",
                        notification.getId(),
                        notification.getType(),
                        userId,
                        e.getMessage());

                notificationService.markFailed(notification);
            }
        }
    }

    private Map<Long, Map<String, Object>> loadNotificationOutboxPayloads(
            List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> aggregateIds =
                notifications.stream()
                        .map(notification -> String.valueOf(notification.getId()))
                        .toList();
        List<OutboxEvent> outboxEvents =
                outboxEventJpaRepository.findLatestByAggregateIds(
                        "NOTIFICATION", "CREATED", aggregateIds);

        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();
        for (OutboxEvent outboxEvent : outboxEvents) {
            Long notificationId = parseLong(outboxEvent.getAggregateId());
            if (notificationId == null || result.containsKey(notificationId)) {
                continue;
            }
            result.put(notificationId, parsePayload(outboxEvent.getPayload()));
        }
        return result;
    }

    private Map<String, Object> parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payloadJson, MAP_TYPE);
        } catch (Exception ex) {
            log.warn("failed to parse notification outbox payload: {}", ex.getMessage());
            return Map.of();
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Map<String, String> buildFcmPayload(
            Notification notification, Map<String, Object> outboxPayload, LocalDateTime now) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put(
                "type",
                safe(notification.getType() == null ? null : notification.getType().name()));
        data.put("notificationId", safe(notification.getId()));
        data.put("title", safe(notification.getTitle()));
        data.put("content", safe(notification.getContent()));
        data.put("sentAt", safe(now == null ? null : ISO_DATE_TIME.format(now)));

        NotificationType type = notification.getType();
        Long targetId = notification.getScheduleId();

        if (type == null) {
            return data;
        }

        switch (type) {
            case CHAT_MESSAGE -> {
                data.put("roomId", safe(resolveOutboxValue(outboxPayload, "room_id", targetId)));
                data.put("messageId", safe(resolveOutboxValue(outboxPayload, "message_id", "")));
                data.put(
                        "unreadCount", safe(resolveOutboxValue(outboxPayload, "unread_count", "")));
                data.put(
                        "senderUserId",
                        safe(resolveOutboxValue(outboxPayload, "sender_user_id", "")));
                data.put(
                        "senderNickname",
                        safe(resolveOutboxValue(outboxPayload, "sender_nickname", "")));
            }
            case REPORT_CREATED -> data.put("reportId", safe(targetId));
            case SCHEDULE_REMINDER -> {
                data.put("scheduleId", safe(targetId));
            }
            case AI_ARRANGE_DONE -> {
                // content + sentAt
            }
            case POST_LIKED -> {
                data.put(
                        "reflectionId",
                        safe(resolveOutboxValue(outboxPayload, "reflection_id", targetId)));
            }
            case FRIEND_REQUESTED, FRIEND_CREATED -> {
                // content + sentAt
            }
        }
        return data;
    }

    private Object resolveOutboxValue(
            Map<String, Object> outboxPayload, String key, Object defaultValue) {
        if (outboxPayload == null || outboxPayload.isEmpty()) {
            return defaultValue;
        }
        return outboxPayload.getOrDefault(key, defaultValue);
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
