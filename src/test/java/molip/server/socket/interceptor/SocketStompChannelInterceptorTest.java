package molip.server.socket.interceptor;

import static org.mockito.Mockito.verify;

import java.util.HashMap;
import molip.server.socket.store.RedisSocketSessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
class SocketStompChannelInterceptorTest {

    private SocketStompChannelInterceptor interceptor;

    @Mock private RedisSocketSessionStore redisSocketSessionStore;
    @Mock private MessageChannel messageChannel;

    @BeforeEach
    void setUp() {
        interceptor = new SocketStompChannelInterceptor(redisSocketSessionStore);
    }

    @Test
    @DisplayName("STOMP DISCONNECT 이면 세션을 삭제한다")
    void disconnectCleanup() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("session-uuid");
        HashMap<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        sessionAttributes.put("deviceId", "device-uuid");
        accessor.setSessionAttributes(sessionAttributes);
        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        interceptor.preSend(message, messageChannel);

        // then
        verify(redisSocketSessionStore).delete("session-uuid", 1L, "device-uuid");
    }
}
