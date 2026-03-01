package molip.server.socket.config;

import lombok.RequiredArgsConstructor;
import molip.server.socket.handler.SocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketHandler socketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://stg.molip.today",
                        "https://stg.molip.today",
                        "http://molip.today",
                        "https://molip.today",
                        "http://127.0.0.1:3000");
    }
}
