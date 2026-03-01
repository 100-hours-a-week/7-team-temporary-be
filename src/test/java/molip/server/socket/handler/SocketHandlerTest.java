package molip.server.socket.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.store.RedisSocketSessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class SocketHandlerTest {

    private TestableSocketHandler socketHandler;

    @Mock private JwtTokenProvider jwtTokenProvider;

    @Mock private JwtUtil jwtUtil;

    @Mock private RedisSocketSessionStore redisSocketSessionStore;

    @Mock private WebSocketSession session;

    @Captor private ArgumentCaptor<TextMessage> textMessageCaptor;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());
    private final Map<String, Object> attributes = new HashMap<>();

    @BeforeEach
    void setUp() {
        socketHandler =
                new TestableSocketHandler(
                        objectMapper, jwtTokenProvider, jwtUtil, redisSocketSessionStore);
    }

    @Test
    @DisplayName("유효한 토큰이면 socket.connected 응답을 반환하고 세션을 저장한다")
    void connectSuccess() throws Exception {
        // given
        String payload =
                """
                {"event":"socket.connect","payload":{"accessToken":"Bearer valid-token","deviceId":"device-uuid"}}
                """;

        given(session.getAttributes()).willReturn(attributes);
        given(session.getId()).willReturn("session-uuid");
        given(jwtTokenProvider.getAccessTokenStatus("valid-token"))
                .willReturn(JwtValidationStatus.VALID);
        given(jwtUtil.extractUserId("valid-token")).willReturn(1L);
        given(jwtUtil.extractDeviceId("valid-token")).willReturn("device-uuid");
        given(redisSocketSessionStore.findSessionId(1L, "device-uuid")).willReturn(null);

        // when
        socketHandler.handle(session, new TextMessage(payload));

        // then
        verify(redisSocketSessionStore).save(eq("session-uuid"), eq(1L), eq("device-uuid"), any());
        verify(session).sendMessage(textMessageCaptor.capture());

        String response = textMessageCaptor.getValue().getPayload();

        assertThat(response).contains("\"event\":\"socket.connected\"");
        assertThat(response).contains("\"sessionId\":\"session-uuid\"");
        assertThat(response).contains("\"userId\":1");
        assertThat(attributes.get("userId")).isEqualTo(1L);
        assertThat(attributes.get("deviceId")).isEqualTo("device-uuid");
    }

    @Test
    @DisplayName("만료된 토큰이면 retryable=true 에러를 반환하고 연결을 종료한다")
    void connectFailsWhenTokenExpired() throws Exception {
        // given
        String payload =
                """
                {"event":"socket.connect","payload":{"accessToken":"Bearer expired-token","deviceId":"device-uuid"}}
                """;

        given(jwtTokenProvider.getAccessTokenStatus("expired-token"))
                .willReturn(JwtValidationStatus.EXPIRED);

        // when
        socketHandler.handle(session, new TextMessage(payload));

        // then
        verify(session).sendMessage(textMessageCaptor.capture());
        verify(session).close(CloseStatus.POLICY_VIOLATION);

        String response = textMessageCaptor.getValue().getPayload();

        assertThat(response).contains("\"event\":\"socket.error\"");
        assertThat(response).contains("\"code\":\"CONNECT_TOKEN_EXPIRED\"");
        assertThat(response).contains("\"retryable\":true");
    }

    @Test
    @DisplayName("같은 기기에 이미 세션이 있으면 중복 세션 에러를 반환한다")
    void connectFailsWhenDuplicateSessionExists() throws Exception {
        // given
        String payload =
                """
                {"event":"socket.connect","payload":{"accessToken":"Bearer valid-token","deviceId":"device-uuid"}}
                """;

        given(session.getId()).willReturn("session-uuid");
        given(jwtTokenProvider.getAccessTokenStatus("valid-token"))
                .willReturn(JwtValidationStatus.VALID);
        given(jwtUtil.extractUserId("valid-token")).willReturn(1L);
        given(jwtUtil.extractDeviceId("valid-token")).willReturn("device-uuid");
        given(redisSocketSessionStore.findSessionId(1L, "device-uuid"))
                .willReturn("another-session");

        // when
        socketHandler.handle(session, new TextMessage(payload));

        // then
        verify(session).sendMessage(textMessageCaptor.capture());
        verify(session).close(CloseStatus.POLICY_VIOLATION);

        String response = textMessageCaptor.getValue().getPayload();

        assertThat(response).contains("\"event\":\"socket.error\"");
        assertThat(response).contains("\"code\":\"CONNECT_DUPLICATE_SESSION\"");
        assertThat(response).contains("\"retryable\":false");
    }

    private static class TestableSocketHandler extends SocketHandler {

        private TestableSocketHandler(
                ObjectMapper objectMapper,
                JwtTokenProvider jwtTokenProvider,
                JwtUtil jwtUtil,
                RedisSocketSessionStore socketSessionStore) {
            super(objectMapper, jwtTokenProvider, jwtUtil, socketSessionStore);
        }

        private void handle(WebSocketSession session, TextMessage message) throws Exception {
            super.handleTextMessage(session, message);
        }
    }
}
