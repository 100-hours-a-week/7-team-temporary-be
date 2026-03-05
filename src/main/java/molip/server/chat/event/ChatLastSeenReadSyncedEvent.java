package molip.server.chat.event;

public record ChatLastSeenReadSyncedEvent(
        Long participantId, Long userId, Long roomId, Long lastSeenMessageId) {}
