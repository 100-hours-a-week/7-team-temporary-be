package molip.server.socket.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class SocketSessionSupport {

    private static final String SOCKET_SESSION_CONTEXT_KEY = "socketSessionContext";

    public void setSessionContext(
            SimpMessageHeaderAccessor headerAccessor, SocketSessionContext sessionContext) {
        Map<String, Object> sessionAttributes = getOrCreateSessionAttributes(headerAccessor);

        sessionAttributes.put(SOCKET_SESSION_CONTEXT_KEY, sessionContext);
    }

    public Optional<SocketSessionContext> getSessionContext(
            SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return Optional.empty();
        }

        Object value = sessionAttributes.get(SOCKET_SESSION_CONTEXT_KEY);
        if (value instanceof SocketSessionContext sessionContext) {
            return Optional.of(sessionContext);
        }

        return Optional.empty();
    }

    private Map<String, Object> getOrCreateSessionAttributes(
            SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            return sessionAttributes;
        }

        Map<String, Object> created = new HashMap<>();
        headerAccessor.setSessionAttributes(created);

        return created;
    }
}
