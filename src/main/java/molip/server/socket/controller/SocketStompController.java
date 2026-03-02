package molip.server.socket.controller;

import lombok.RequiredArgsConstructor;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketDisconnectRequest;
import molip.server.socket.dto.request.SocketUserSubscribeRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.service.SocketHandshakeService;
import molip.server.socket.session.SocketSessionSupport;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SocketStompController {

    private final SocketHandshakeService socketHandshakeService;
    private final SocketSessionSupport socketSessionSupport;

    @MessageMapping("/handshake/connect")
    @SendToUser(value = "/queue/handshake", broadcast = false)
    public SocketEventResponse<?> connect(
            SocketConnectRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        return socketHandshakeService.connect(request, sessionId, headerAccessor);
    }

    @MessageMapping("/handshake/disconnect")
    public void disconnect(
            SocketDisconnectRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        if (request == null || request.code() == null || request.code().isBlank()) {
            return;
        }

        socketSessionSupport
                .getSessionContext(headerAccessor)
                .ifPresent(
                        sessionContext ->
                                socketHandshakeService.disconnect(sessionId, sessionContext));
    }

    @MessageMapping("/user/subscribe")
    @SendToUser(value = "/queue/user", broadcast = false)
    public SocketEventResponse<?> subscribeUser(
            SocketUserSubscribeRequest request, SimpMessageHeaderAccessor headerAccessor) {
        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketHandshakeService.subscribeUser(request, sessionContext))
                .orElseGet(socketHandshakeService::invalidSubscribeState);
    }
}
