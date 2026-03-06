package molip.server.notification.event;

public record FriendRequestedEvent(Long targetUserId, String requesterNickname) {}
