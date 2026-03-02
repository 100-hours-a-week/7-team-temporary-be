package molip.server.socket.listener;

import static org.mockito.BDDMockito.then;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import molip.server.socket.dto.response.SocketConnectedResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@ExtendWith(MockitoExtension.class)
class SocketStompSessionListenerTest {

    private SocketStompSessionListener listener;

    @Mock private SimpMessagingTemplate simpMessagingTemplate;

    @Captor private ArgumentCaptor<SocketEventResponse<?>> payloadCaptor;

    @BeforeEach
    void setUp() {
        listener = new SocketStompSessionListener(simpMessagingTemplate);
    }

    @Test
    @DisplayName("SessionConnectedEvent가 오면 socket.connected를 개인 채널로 전송한다")
    void sendsSocketConnected() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
        accessor.setSessionId("session-uuid");
        HashMap<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", 1L);
        sessionAttributes.put("connectedAt", "2026-01-13T10:10:10+09:00");
        accessor.setSessionAttributes(sessionAttributes);
        Principal principal =
                new UsernamePasswordAuthenticationToken("1", null, List.of());
        accessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SessionConnectedEvent event = new SessionConnectedEvent(new Object(), message);

        // when
        listener.handleSessionConnected(event);

        // then
        then(simpMessagingTemplate)
                .should()
                .convertAndSendToUser(org.mockito.ArgumentMatchers.eq("1"), org.mockito.ArgumentMatchers.eq("/queue/handshake"), payloadCaptor.capture());

        SocketEventResponse<?> payload = payloadCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(payload.event()).isEqualTo("socket.connected");
        org.assertj.core.api.Assertions.assertThat(payload.payload()).isInstanceOf(SocketConnectedResponse.class);
    }
}
