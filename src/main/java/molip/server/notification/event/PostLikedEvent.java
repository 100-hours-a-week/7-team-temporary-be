package molip.server.notification.event;

public record PostLikedEvent(Long targetUserId, Long reflectionId, String likerNickname) {}
