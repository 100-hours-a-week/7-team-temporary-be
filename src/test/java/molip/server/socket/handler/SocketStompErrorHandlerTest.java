package molip.server.socket.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import molip.server.socket.dto.response.SocketErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class SocketStompErrorHandlerTest {

    private SocketStompErrorHandler socketStompErrorHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        socketStompErrorHandler = new SocketStompErrorHandler(objectMapper);
    }

    @Test
    @DisplayName("토큰 만료 예외면 retryable=true 인 STOMP ERROR 프레임을 생성한다")
    void shouldCreateRetryableStompErrorFrameWhenTokenExpired() throws Exception {
        // given
        Message<byte[]> clientMessage = MessageBuilder.withPayload(new byte[0]).build();
        MessagingException exception =
                new MessagingException("CONNECT_TOKEN_EXPIRED: 액세스 토큰이 만료되었습니다.");

        // when
        Message<byte[]> errorMessage =
                socketStompErrorHandler.handleClientMessageProcessingError(
                        clientMessage, exception);

        // then
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(errorMessage);
        SocketErrorResponse response =
                objectMapper.readValue(errorMessage.getPayload(), SocketErrorResponse.class);

        assertThat(accessor.getCommand())
                .isEqualTo(org.springframework.messaging.simp.stomp.StompCommand.ERROR);
        assertThat(accessor.getMessage()).isEqualTo("액세스 토큰이 만료되었습니다.");
        assertThat(response.code()).isEqualTo("CONNECT_TOKEN_EXPIRED");
        assertThat(response.message()).isEqualTo("액세스 토큰이 만료되었습니다.");
        assertThat(response.retryable()).isTrue();
    }

    @Test
    @DisplayName("중복 세션 예외면 retryable=false 인 STOMP ERROR 프레임을 생성한다")
    void shouldCreateNonRetryableStompErrorFrameWhenDuplicateSession() throws Exception {
        // given
        Message<byte[]> clientMessage = MessageBuilder.withPayload(new byte[0]).build();
        MessagingException exception =
                new MessagingException("CONNECT_DUPLICATE_SESSION: 이미 연결된 세션이 존재합니다.");

        // when
        Message<byte[]> errorMessage =
                socketStompErrorHandler.handleClientMessageProcessingError(
                        clientMessage, exception);

        // then
        SocketErrorResponse response =
                objectMapper.readValue(errorMessage.getPayload(), SocketErrorResponse.class);

        assertThat(response.code()).isEqualTo("CONNECT_DUPLICATE_SESSION");
        assertThat(response.message()).isEqualTo("이미 연결된 세션이 존재합니다.");
        assertThat(response.retryable()).isFalse();
    }
}
