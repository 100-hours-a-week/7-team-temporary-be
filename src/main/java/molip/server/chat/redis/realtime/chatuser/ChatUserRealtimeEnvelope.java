package molip.server.chat.redis.realtime.chatuser;

public record ChatUserRealtimeEnvelope(String eventType, Long userId, String payloadJson) {

    public static ChatUserRealtimeEnvelope of(String eventType, Long userId, String payloadJson) {
        return new ChatUserRealtimeEnvelope(eventType, userId, payloadJson);
    }
}
