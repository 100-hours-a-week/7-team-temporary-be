package molip.server.chat.dto.response;

public record ChatLastSeenUpdatedResponse(
        String eventId, Long roomId, Long participantId, Long userId, Long lastSeenMessageId) {

    public static ChatLastSeenUpdatedResponse of(
            String eventId, Long roomId, Long participantId, Long userId, Long lastSeenMessageId) {
        return new ChatLastSeenUpdatedResponse(
                eventId, roomId, participantId, userId, lastSeenMessageId);
    }
}
