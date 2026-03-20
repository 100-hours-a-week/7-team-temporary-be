package molip.server.chat.service.video;

public record LiveKitTokenCommand(
        String roomName,
        String identity,
        String displayName,
        boolean canPublish,
        boolean canSubscribe,
        long ttlSeconds) {

    public static LiveKitTokenCommand of(
            String roomName,
            String identity,
            String displayName,
            boolean canPublish,
            boolean canSubscribe,
            long ttlSeconds) {
        return new LiveKitTokenCommand(
                roomName, identity, displayName, canPublish, canSubscribe, ttlSeconds);
    }
}
