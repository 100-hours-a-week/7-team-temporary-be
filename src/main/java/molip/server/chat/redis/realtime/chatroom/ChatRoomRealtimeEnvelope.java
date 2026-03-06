package molip.server.chat.redis.realtime.chatroom;

public record ChatRoomRealtimeEnvelope(String eventType, Long roomId, String payloadJson) {

    public static ChatRoomRealtimeEnvelope of(String eventType, Long roomId, String payloadJson) {
        return new ChatRoomRealtimeEnvelope(eventType, roomId, payloadJson);
    }
}
