package molip.server.socket.controller;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.UpdateLastReadMessageRequest;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.socket.dto.request.SocketConnectRequest;
import molip.server.socket.dto.request.SocketDisconnectRequest;
import molip.server.socket.dto.request.SocketLastSeenUpdateRequest;
import molip.server.socket.dto.request.SocketMessageSendRequest;
import molip.server.socket.dto.request.SocketRoomSubscribeRequest;
import molip.server.socket.dto.request.SocketRoomUnsubscribeRequest;
import molip.server.socket.dto.request.SocketUserSubscribeRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketMessageSendRejectedResponse;
import molip.server.socket.service.SocketHandshakeService;
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

    @MessageMapping("/room/subscribe")
    @SendToUser(value = "/queue/room", broadcast = false)
    public SocketEventResponse<?> subscribeRoom(
            SocketRoomSubscribeRequest request, SimpMessageHeaderAccessor headerAccessor) {

        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketRoomSubscriptionService.subscribeRoom(
                                        request, sessionContext, headerAccessor))
                .orElseGet(socketRoomSubscriptionService::invalidSubscribeState);
    }

    @MessageMapping("/room/unsubscribe")
    public void unsubscribeRoom(
            SocketRoomUnsubscribeRequest request, SimpMessageHeaderAccessor headerAccessor) {
        if (request == null) {
            return;
        }

        socketSessionSupport
                .getSessionContext(headerAccessor)
                .ifPresent(
                        sessionContext ->
                                socketRoomSubscriptionService.unsubscribeRoom(
                                        request, sessionContext, headerAccessor));
    }

    @MessageMapping("/room/last-seen")
    public void updateLastSeenMessage(
            SocketLastSeenUpdateRequest request, SimpMessageHeaderAccessor headerAccessor) {
        if (request == null) {
            return;
        }

        socketSessionSupport
                .getSessionContext(headerAccessor)
                .ifPresent(
                        sessionContext ->
                                chatRoomCommandFacade.updateLastSeenMessage(
                                        sessionContext.userId(),
                                        request.participantId(),
                                        new UpdateLastReadMessageRequest(
                                                request.lastSeenMessageId())));
    }

    @MessageMapping("/room/message")
    @SendToUser(value = "/queue/room", broadcast = false)
    public SocketEventResponse<?> sendMessage(
            SocketMessageSendRequest request, SimpMessageHeaderAccessor headerAccessor) {

        return socketSessionSupport
                .getSessionContext(headerAccessor)
                .<SocketEventResponse<?>>map(
                        sessionContext ->
                                socketRoomMessageService.sendMessage(
                                        sessionContext.userId(), request))
                .orElseGet(() -> invalidSendState(request));
    }

    private SocketEventResponse<SocketMessageSendRejectedResponse> invalidSendState(
            SocketMessageSendRequest request) {
        return SocketEventResponse.of(
                "message.sendRejected",
                SocketMessageSendRejectedResponse.of(
                        request == null ? null : request.idempotencyKey(),
                        "MESSAGE_SEND_FORBIDDEN",
                        "인증 완료 후에만 메시지를 전송할 수 있습니다.",
                        false));
    }
}
