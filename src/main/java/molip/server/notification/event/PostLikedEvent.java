package molip.server.notification.event;

public record PostLikedEvent(Long targetUserId, String likerNickname) {}
