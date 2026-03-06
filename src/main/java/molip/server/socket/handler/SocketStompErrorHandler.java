package molip.server.socket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import molip.server.socket.dto.response.SocketErrorResponse;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@RequiredArgsConstructor
public class SocketStompErrorHandler extends StompSubProtocolErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            @Nullable Message<byte[]> clientMessage, Throwable ex) {
        SocketErrorResponse errorResponse = createErrorResponse(ex);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorResponse.message());
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(serialize(errorResponse), accessor.getMessageHeaders());
    }

    private SocketErrorResponse createErrorResponse(Throwable ex) {
        String rawMessage = ex.getMessage();
        if (rawMessage == null || rawMessage.isBlank()) {
            return SocketErrorResponse.of("CONNECT_INTERNAL_ERROR", "소켓 연결 중 오류가 발생했습니다.", true);
        }

        int separatorIndex = rawMessage.indexOf(':');
        if (separatorIndex < 0) {
            return SocketErrorResponse.of("CONNECT_INTERNAL_ERROR", rawMessage, true);
        }

        String code = rawMessage.substring(0, separatorIndex).trim();
        String message = rawMessage.substring(separatorIndex + 1).trim();
        return SocketErrorResponse.of(code, message, isRetryable(code));
    }

    private boolean isRetryable(String code) {
        return "CONNECT_TOKEN_EXPIRED".equals(code) || "CONNECT_INTERNAL_ERROR".equals(code);
    }

    private byte[] serialize(SocketErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            return ("{\"code\":\"CONNECT_INTERNAL_ERROR\",\"message\":\"소켓 연결 중 오류가 발생했습니다.\",\"retryable\":true}")
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
