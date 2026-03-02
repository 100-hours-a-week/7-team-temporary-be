package molip.server.socket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.util.HashMap;
import molip.server.auth.jwt.JwtTokenProvider;
import molip.server.auth.jwt.JwtUtil;
import molip.server.auth.jwt.JwtValidationStatus;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketDisconnectRequest;
import molip.server.socket.dto.response.SocketConnectedResponse;
import molip.server.socket.dto.response.SocketErrorResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.store.RedisSocketSessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

@ExtendWith(MockitoExtension.class)
class SocketStompControllerTest {

    private SocketStompController socketStompController;

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private JwtUtil jwtUtil;
    @Mock private RedisSocketSessionStore socketSessionStore;

    @BeforeEach
    void setUp() {
        socketStompController =
                new SocketStompController(jwtTokenProvider, jwtUtil, socketSessionStore);
    }

    @Test
    @DisplayName("유효한 socket.connect 요청이면 socket.connected를 반환하고 세션을 저장한다")
    void connectSuccess() {
        // given
        SocketConnectRequest request =
                new SocketConnectRequest("Bearer valid-token", "device-uuid");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());

        given(jwtTokenProvider.getAccessTokenStatus("valid-token"))
                .willReturn(JwtValidationStatus.VALID);
        given(jwtUtil.extractUserId("valid-token")).willReturn(1L);
        given(jwtUtil.extractDeviceId("valid-token")).willReturn("device-uuid");
        given(socketSessionStore.findSessionId(1L, "device-uuid")).willReturn(null);

        // when
        SocketEventResponse<?> response =
                socketStompController.connect(request, "session-uuid", headerAccessor);

        // then
        assertThat(response.event()).isEqualTo("socket.connected");
        assertThat(response.payload()).isInstanceOf(SocketConnectedResponse.class);
        assertThat(headerAccessor.getSessionAttributes().get("userId")).isEqualTo(1L);
        verify(socketSessionStore)
                .save(
                        org.mockito.ArgumentMatchers.eq("session-uuid"),
                        org.mockito.ArgumentMatchers.eq(1L),
                        org.mockito.ArgumentMatchers.eq("device-uuid"),
                        org.mockito.ArgumentMatchers.any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("만료된 토큰이면 socket.error를 반환한다")
    void connectFailsWhenTokenExpired() {
        // given
        SocketConnectRequest request =
                new SocketConnectRequest("Bearer expired-token", "device-uuid");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());

        given(jwtTokenProvider.getAccessTokenStatus("expired-token"))
                .willReturn(JwtValidationStatus.EXPIRED);

        // when
        SocketEventResponse<?> response =
                socketStompController.connect(request, "session-uuid", headerAccessor);

        // then
        assertThat(response.event()).isEqualTo("socket.error");
        assertThat(response.payload()).isInstanceOf(SocketErrorResponse.class);
        SocketErrorResponse error = (SocketErrorResponse) response.payload();
        assertThat(error.code()).isEqualTo("CONNECT_TOKEN_EXPIRED");
        assertThat(error.retryable()).isTrue();
    }

    @Test
    @DisplayName("중복 세션이면 socket.error를 반환한다")
    void connectFailsWhenDuplicateSessionExists() {
        // given
        SocketConnectRequest request =
                new SocketConnectRequest("Bearer valid-token", "device-uuid");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());

        given(jwtTokenProvider.getAccessTokenStatus("valid-token"))
                .willReturn(JwtValidationStatus.VALID);
        given(jwtUtil.extractUserId("valid-token")).willReturn(1L);
        given(jwtUtil.extractDeviceId("valid-token")).willReturn("device-uuid");
        given(socketSessionStore.findSessionId(1L, "device-uuid")).willReturn("existing-session");

        // when
        SocketEventResponse<?> response =
                socketStompController.connect(request, "session-uuid", headerAccessor);

        // then
        assertThat(response.event()).isEqualTo("socket.error");
        SocketErrorResponse error = (SocketErrorResponse) response.payload();
        assertThat(error.code()).isEqualTo("CONNECT_DUPLICATE_SESSION");
        assertThat(error.retryable()).isFalse();
    }

    @Test
    @DisplayName("명시적 socket.disconnect 요청이면 세션을 삭제한다")
    void disconnectDeletesSocketSession() {
        // given
        SocketDisconnectRequest request =
                new SocketDisconnectRequest("LOGOUT", "로그아웃으로 연결을 종료합니다.");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        HashMap<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        sessionAttributes.put("deviceId", "device-uuid");
        headerAccessor.setSessionAttributes(sessionAttributes);

        // when
        socketStompController.disconnect(request, "session-uuid", headerAccessor);

        // then
        verify(socketSessionStore).delete("session-uuid", 1L, "device-uuid");
    }
}
