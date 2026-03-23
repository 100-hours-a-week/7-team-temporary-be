package molip.server.notification.event;

public record ReportCreatedEvent(Long targetUserId, Long reportId) {}
