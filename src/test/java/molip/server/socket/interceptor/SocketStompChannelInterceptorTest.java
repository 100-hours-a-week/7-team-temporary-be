package molip.server.socket.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.store.RedisSocketSessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
class SocketStompChannelInterceptorTest {

    private SocketStompChannelInterceptor interceptor;

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private JwtUtil jwtUtil;
    @Mock private RedisSocketSessionStore redisSocketSessionStore;
    @Mock private MessageChannel messageChannel;

    @BeforeEach
    void setUp() {
        interceptor = new SocketStompChannelInterceptor(jwtTokenProvider, jwtUtil, redisSocketSessionStore);
    }

    @Test
    @DisplayName("STOMP CONNECT 헤더가 유효하면 세션을 저장하고 Principal을 바인딩한다")
    void connectSuccess() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("session-uuid");
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setNativeHeader("accessToken", "Bearer valid-token");
        accessor.setNativeHeader("deviceId", "device-uuid");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        given(jwtTokenProvider.getAccessTokenStatus("valid-token")).willReturn(JwtValidationStatus.VALID);
        given(jwtUtil.extractUserId("valid-token")).willReturn(1L);
        given(jwtUtil.extractDeviceId("valid-token")).willReturn("device-uuid");
        given(redisSocketSessionStore.findSessionId(1L, "device-uuid")).willReturn(null);

        // when
        Message<?> result = interceptor.preSend(message, messageChannel);

        // then
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("1");
        assertThat(resultAccessor.getSessionAttributes().get("userId")).isEqualTo(1L);
        verify(redisSocketSessionStore).save(org.mockito.ArgumentMatchers.eq("session-uuid"), org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("device-uuid"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("만료된 토큰이면 CONNECT 단계에서 예외를 던진다")
    void connectFailsWhenTokenExpired() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId("session-uuid");
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setNativeHeader("accessToken", "Bearer expired-token");
        accessor.setNativeHeader("deviceId", "device-uuid");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        given(jwtTokenProvider.getAccessTokenStatus("expired-token")).willReturn(JwtValidationStatus.EXPIRED);

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("CONNECT_TOKEN_EXPIRED");
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
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        interceptor.preSend(message, messageChannel);

        // then
        verify(redisSocketSessionStore).delete("session-uuid", 1L, "device-uuid");
    }
}
