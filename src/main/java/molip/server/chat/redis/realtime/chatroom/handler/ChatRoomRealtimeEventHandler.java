package molip.server.chat.redis.realtime.chatroom.handler;

import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimeEnvelope;

public interface ChatRoomRealtimeEventHandler {

    String eventType();

    void handle(ChatRoomRealtimeEnvelope envelope);
}
