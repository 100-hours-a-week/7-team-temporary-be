package molip.server.notification.event;

public record ChatMessageNotificationRequestedEvent(
        Long targetUserId, Long roomId, String senderNickname, String messagePreview) {}
