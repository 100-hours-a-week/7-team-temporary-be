package molip.server.chat.config;

import lombok.RequiredArgsConstructor;
import molip.server.chat.realtime.ChatRoomRealtimePublisher;
import molip.server.chat.realtime.ChatRoomRealtimeSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class ChatRedisPubSubConfig {

    private final ChatRoomRealtimeSubscriber chatRoomRealtimeSubscriber;

    @Bean
    public ChannelTopic chatRoomEventsTopic() {
        return new ChannelTopic(ChatRoomRealtimePublisher.CHAT_ROOM_EVENTS_TOPIC);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory, ChannelTopic chatRoomEventsTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(chatRoomRealtimeSubscriber, chatRoomEventsTopic);

        return container;
    }
}
