package molip.server.chat.redis.realtime.chatuser.handler;

import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimeEnvelope;

public interface ChatUserRealtimeEventHandler {

    String eventType();

    void handle(ChatUserRealtimeEnvelope envelope);
}
