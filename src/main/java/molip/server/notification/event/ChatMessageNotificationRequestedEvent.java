package molip.server.notification.event;

public record ChatMessageNotificationRequestedEvent(
        Long targetUserId,
        Long roomId,
        Long messageId,
        Integer unreadCount,
        Long senderUserId,
        String senderNickname,
        String messagePreview) {}
