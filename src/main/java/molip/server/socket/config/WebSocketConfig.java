package molip.server.socket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import molip.server.socket.handler.SocketStompErrorHandler;
import molip.server.socket.interceptor.SocketHttpHandshakeInterceptor;
import molip.server.socket.interceptor.SocketStompChannelInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SocketHttpHandshakeInterceptor socketHttpHandshakeInterceptor;
    private final SocketStompChannelInterceptor socketStompChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws", "/api/chat/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://stg.molip.today",
                        "https://stg.molip.today",
                        "http://molip.today",
                        "https://molip.today",
                        "http://127.0.0.1:3000")
                .addInterceptors(socketHttpHandshakeInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableSimpleBroker("/sub", "/queue")
                .setTaskScheduler(webSocketHeartbeatTaskScheduler())
                .setHeartbeatValue(new long[] {10000, 10000});
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(socketStompChannelInterceptor);
    }

    @Bean
    public SocketStompErrorHandler stompSubProtocolErrorHandler(ObjectMapper objectMapper) {
        return new SocketStompErrorHandler(objectMapper);
    }

    @Bean
    public TaskScheduler webSocketHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.initialize();

        return scheduler;
    }
}
