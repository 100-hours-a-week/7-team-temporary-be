package molip.server.socket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Optional;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketDisconnectRequest;
import molip.server.socket.dto.request.SocketUserSubscribeRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.service.SocketHandshakeService;
import molip.server.socket.session.SocketSessionContext;
import molip.server.socket.session.SocketSessionSupport;
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

    @Mock private SocketHandshakeService socketHandshakeService;
    @Mock private SocketSessionSupport socketSessionSupport;

    @BeforeEach
    void setUp() {
        socketStompController =
                new SocketStompController(socketHandshakeService, socketSessionSupport);
    }

    @Test
    @DisplayName("socket.connect 요청은 handshake service에 위임한다")
    void connectDelegatesToHandshakeService() {
        // given
        SocketConnectRequest request =
                new SocketConnectRequest("Bearer valid-token", "device-uuid");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        SocketEventResponse<?> expected = SocketEventResponse.of("socket.connected", "ok");

        doReturn(expected)
                .when(socketHandshakeService)
                .connect(request, "session-uuid", headerAccessor);

        // when
        SocketEventResponse<?> response =
                socketStompController.connect(request, "session-uuid", headerAccessor);

        // then
        assertThat(response).isEqualTo(expected);
        verify(socketHandshakeService).connect(request, "session-uuid", headerAccessor);
    }

    @Test
    @DisplayName("명시적 socket.disconnect 요청이면 세션 컨텍스트가 있을 때 handshake service에 위임한다")
    void disconnectDelegatesWhenSessionContextExists() {
        // given
        SocketDisconnectRequest request =
                new SocketDisconnectRequest("LOGOUT", "로그아웃으로 연결을 종료합니다.");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        SocketSessionContext sessionContext =
                SocketSessionContext.of(
                        "Bearer valid-token", "device-uuid", 1L, OffsetDateTime.now());

        given(socketSessionSupport.getSessionContext(headerAccessor))
                .willReturn(Optional.of(sessionContext));

        // when
        socketStompController.disconnect(request, "session-uuid", headerAccessor);

        // then
        verify(socketHandshakeService).disconnect("session-uuid", sessionContext);
    }

    @Test
    @DisplayName("subscribe.user는 인증 완료 세션이면 handshake service에 위임한다")
    void subscribeUserDelegatesWhenAuthenticated() {
        // given
        SocketUserSubscribeRequest request = new SocketUserSubscribeRequest(OffsetDateTime.now());
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        SocketSessionContext sessionContext =
                SocketSessionContext.of(
                        "Bearer valid-token", "device-uuid", 1L, OffsetDateTime.now());
        SocketEventResponse<?> expected = SocketEventResponse.of("subscribed.user", "ok");

        given(socketSessionSupport.getSessionContext(headerAccessor))
                .willReturn(Optional.of(sessionContext));
        doReturn(expected).when(socketHandshakeService).subscribeUser(request, sessionContext);

        // when
        SocketEventResponse<?> response =
                socketStompController.subscribeUser(request, headerAccessor);

        // then
        assertThat(response).isEqualTo(expected);
        verify(socketHandshakeService).subscribeUser(request, sessionContext);
    }
}
