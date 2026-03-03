package molip.server.chat.config;

import lombok.RequiredArgsConstructor;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimePublisher;
import molip.server.chat.redis.realtime.chatroom.ChatRoomRealtimeSubscriber;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimePublisher;
import molip.server.chat.redis.realtime.chatuser.ChatUserRealtimeSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true")
public class ChatRedisPubSubConfig {

    private final ChatRoomRealtimeSubscriber chatRoomRealtimeSubscriber;
    private final ChatUserRealtimeSubscriber chatUserRealtimeSubscriber;

    @Bean
    public ChannelTopic chatRoomEventsTopic() {
        return new ChannelTopic(ChatRoomRealtimePublisher.CHAT_ROOM_EVENTS_TOPIC);
    }

    @Bean
    public ChannelTopic chatUserEventsTopic() {
        return new ChannelTopic(ChatUserRealtimePublisher.CHAT_USER_EVENTS_TOPIC);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            ChannelTopic chatRoomEventsTopic,
            ChannelTopic chatUserEventsTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(chatRoomRealtimeSubscriber, chatRoomEventsTopic);
        container.addMessageListener(chatUserRealtimeSubscriber, chatUserEventsTopic);

        return container;
    }
}
