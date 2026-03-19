package molip.server.socket.interceptor;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class SocketHttpHandshakeInterceptor implements HandshakeInterceptor {

    private static final String ACCESS_TOKEN_SESSION_KEY = "accessToken";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String token = resolveAccessToken(request);
        if (token != null) {
            attributes.put(ACCESS_TOKEN_SESSION_KEY, token);
        }

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {}

    private String resolveAccessToken(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return null;
        }

        String cookieHeader = servletRequest.getServletRequest().getHeader("Cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String[] cookiePairs = cookieHeader.split(";");
        for (String cookiePair : cookiePairs) {
            String[] nameValue = cookiePair.trim().split("=", 2);
            if (nameValue.length == 2
                    && ACCESS_TOKEN_COOKIE.equals(nameValue[0].trim())
                    && !nameValue[1].trim().isBlank()) {
                return nameValue[1].trim();
            }
        }

        return null;
    }
}
