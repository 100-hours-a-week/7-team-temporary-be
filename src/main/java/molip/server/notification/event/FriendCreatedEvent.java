package molip.server.notification.event;

public record FriendCreatedEvent(Long targetUserId, String accepterNickname) {}
