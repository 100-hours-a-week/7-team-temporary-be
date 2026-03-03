package molip.server.socket.session;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class SocketSessionSupport {

    private static final String SOCKET_SESSION_CONTEXT_KEY = "socketSessionContext";
    private static final String SUBSCRIBED_ROOM_IDS_KEY = "subscribedRoomIds";
    private static final String LAST_PONG_AT_KEY = "lastPongAt";

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

        return Optional.ofNullable(sessionAttributes.get(SOCKET_SESSION_CONTEXT_KEY))
                .filter(SocketSessionContext.class::isInstance)
                .map(SocketSessionContext.class::cast);
    }

    public void subscribeRoom(SimpMessageHeaderAccessor headerAccessor, Long roomId) {
        Set<Long> subscribedRoomIds = getOrCreateSubscribedRoomIds(headerAccessor);

        subscribedRoomIds.add(roomId);
    }

    public boolean unsubscribeRoom(SimpMessageHeaderAccessor headerAccessor, Long roomId) {
        Set<Long> subscribedRoomIds = getOrCreateSubscribedRoomIds(headerAccessor);

        return subscribedRoomIds.remove(roomId);
    }

    public void updateLastPongAt(
            SimpMessageHeaderAccessor headerAccessor, OffsetDateTime lastPongAt) {
        Map<String, Object> sessionAttributes = getOrCreateSessionAttributes(headerAccessor);

        sessionAttributes.put(LAST_PONG_AT_KEY, lastPongAt);
    }

    public Optional<OffsetDateTime> getLastPongAt(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(sessionAttributes.get(LAST_PONG_AT_KEY))
                .filter(OffsetDateTime.class::isInstance)
                .map(OffsetDateTime.class::cast);
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

    @SuppressWarnings("unchecked")
    private Set<Long> getOrCreateSubscribedRoomIds(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = getOrCreateSessionAttributes(headerAccessor);

        Set<Long> subscribedRoomIds =
                Optional.ofNullable(sessionAttributes.get(SUBSCRIBED_ROOM_IDS_KEY))
                        .filter(Set.class::isInstance)
                        .map(value -> (Set<Long>) value)
                        .orElse(null);

        if (subscribedRoomIds != null) {
            return subscribedRoomIds;
        }

        Set<Long> created = new HashSet<>();
        sessionAttributes.put(SUBSCRIBED_ROOM_IDS_KEY, created);

        return created;
    }
}
