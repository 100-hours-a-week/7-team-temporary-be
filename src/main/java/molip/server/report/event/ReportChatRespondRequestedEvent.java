package molip.server.report.event;

public record ReportChatRespondRequestedEvent(Long reportId, Long streamMessageId) {}
