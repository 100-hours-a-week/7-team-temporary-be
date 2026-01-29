package molip.server.notification.event;

import java.time.LocalDate;
import java.time.LocalTime;

public record NotificationCreatedEvent(
        Long scheduleId, Long userId, String title, LocalDate planDate, LocalTime startAt) {}
