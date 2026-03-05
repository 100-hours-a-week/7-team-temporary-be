package molip.server.socket.controller;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketDisconnectRequest;
import molip.server.socket.dto.request.SocketLastSeenUpdateRequest;
import molip.server.socket.dto.request.SocketMessageSendRequest;
import molip.server.socket.dto.request.SocketPongRequest;
import molip.server.socket.dto.request.SocketReportMessageSendRequest;
import molip.server.socket.dto.request.SocketRoomSubscribeRequest;
import molip.server.socket.dto.request.SocketRoomUnsubscribeRequest;
import molip.server.socket.dto.request.SocketUserSubscribeRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.service.SocketHandshakeService;
import molip.server.socket.service.SocketReportMessageService;
import molip.server.socket.service.SocketRoomMessageService;
import molip.server.socket.service.SocketRoomSubscriptionService;
import molip.server.socket.session.SocketSessionSupport;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SocketStompController {

    private final ChatRoomCommandFacade chatRoomCommandFacade;
    private final SocketHandshakeService socketHandshakeService;
    private final SocketRoomSubscriptionService socketRoomSubscriptionService;
    private final SocketRoomMessageService socketRoomMessageService;
    private final SocketReportMessageService socketReportMessageService;
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

    @MessageMapping("/handshake/pong")
    public void pong(
            SocketPongRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        socketSessionSupport
                .getSessionContext(headerAccessor)
                .ifPresentOrElse(
                        sessionContext -> socketHandshakeService.pong(request, headerAccessor),
                        () -> socketHandshakeService.requireReconnect(sessionId));
    }

    @MessageMapping("/user/subscribe")
    @SendToUser(value = "/queue/user", broadcast = false)
    public SocketEventResponse<?> subscribeUser(
            SocketUserSubscribeRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketHandshakeService.subscribeUser(request, sessionContext))
                .orElseGet(() -> reconnectRequired(sessionId));
    }

    @MessageMapping("/room/subscribe")
    @SendToUser(value = "/queue/room", broadcast = false)
    public SocketEventResponse<?> subscribeRoom(
            SocketRoomSubscribeRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketRoomSubscriptionService.subscribeRoom(
                                        request, sessionContext, headerAccessor))
                .orElseGet(() -> reconnectRequired(sessionId));
    }

    @MessageMapping("/room/unsubscribe")
    public void unsubscribeRoom(
            SocketRoomUnsubscribeRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        if (request == null) {
            return;
        }

        socketSessionSupport
                .getSessionContext(headerAccessor)
                .ifPresentOrElse(
                        sessionContext ->
                                socketRoomSubscriptionService.unsubscribeRoom(
                                        request, sessionContext, headerAccessor),
                        () -> socketHandshakeService.requireReconnect(sessionId));
    }

    @MessageMapping("/room/last-seen")
    public void updateLastSeenMessage(
            SocketLastSeenUpdateRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {
        if (request == null) {
            return;
        }

        socketSessionSupport
                .getSessionContext(headerAccessor)
                .ifPresentOrElse(
                        sessionContext ->
                                chatRoomCommandFacade.updateLastSeenMessage(
                                        sessionContext.userId(),
                                        request.participantId(),
                                        new UpdateLastReadMessageRequest(
                                                request.lastSeenMessageId())),
                        () -> socketHandshakeService.requireReconnect(sessionId));
    }

    @MessageMapping("/room/message")
    @SendToUser(value = "/queue/room", broadcast = false)
    public SocketEventResponse<?> sendMessage(
            SocketMessageSendRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketRoomMessageService.sendMessage(
                                        sessionContext.userId(), request))
                .orElseGet(() -> reconnectRequired(sessionId));
    }

    @MessageMapping("/report/message")
    @SendToUser(value = "/queue/report", broadcast = false)
    public SocketEventResponse<?> sendReportMessage(
            SocketReportMessageSendRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketReportMessageService.sendMessage(
                                        sessionContext.userId(), request))
                .orElseGet(() -> reconnectRequired(sessionId));
    }

    private SocketEventResponse<?> reconnectRequired(String sessionId) {
        socketHandshakeService.requireReconnect(sessionId);

        return null;
    }
}
