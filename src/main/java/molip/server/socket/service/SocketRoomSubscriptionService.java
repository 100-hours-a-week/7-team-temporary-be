package molip.server.socket.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.socket.dto.request.SocketRoomSubscribeRequest;
import molip.server.socket.dto.request.SocketRoomUnsubscribeRequest;
import molip.server.socket.dto.response.SocketErrorResponse;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketSubscribedRoomResponse;
import molip.server.socket.session.SocketSessionContext;
import molip.server.socket.session.SocketSessionSupport;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketRoomSubscriptionService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SOCKET_ERROR_EVENT = "socket.error";
    private static final String SUBSCRIBED_ROOM_EVENT = "subscribed.room";

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final SocketSessionSupport socketSessionSupport;

    public SocketEventResponse<?> subscribeRoom(
            SocketRoomSubscribeRequest request,
            SocketSessionContext sessionContext,
            SimpMessageHeaderAccessor headerAccessor) {

        SocketEventResponse<?> invalidRequestResponse =
                validateSubscribeRoomRequest(request, sessionContext);

        if (invalidRequestResponse != null) {
            return invalidRequestResponse;
        }

        socketSessionSupport.subscribeRoom(headerAccessor, request.roomId());

        return SocketEventResponse.of(
                SUBSCRIBED_ROOM_EVENT,
                SocketSubscribedRoomResponse.of(
                        request.roomId(),
                        request.participantId(),
                        OffsetDateTime.now(KOREA_ZONE_ID)));
    }

    public void unsubscribeRoom(
            SocketRoomUnsubscribeRequest request,
            SocketSessionContext sessionContext,
            SimpMessageHeaderAccessor headerAccessor) {

        Long roomId = validateUnsubscribeRoomRequest(request, sessionContext);

        if (roomId == null) {
            return;
        }

        socketSessionSupport.unsubscribeRoom(headerAccessor, roomId);
    }

    public SocketEventResponse<SocketErrorResponse> invalidSubscribeState() {
        return error("ROOM_SUBSCRIBE_INVALID_STATE", "인증 완료 후에만 방 채널을 구독할 수 있습니다.", false);
    }

    private SocketEventResponse<?> validateSubscribeRoomRequest(
            SocketRoomSubscribeRequest request, SocketSessionContext sessionContext) {
        if (request == null || request.roomId() == null || request.participantId() == null) {
            return error("ROOM_SUBSCRIBE_INVALID_STATE", "구독 요청 정보가 올바르지 않습니다.", false);
        }

        ChatRoomParticipant participant = getParticipant(request.participantId());

        if (participant == null) {
            return error("ROOM_SUBSCRIBE_NOT_FOUND", "참가자 정보를 찾을 수 없습니다.", false);
        }

        if (!participant.getChatRoom().getId().equals(request.roomId())) {
            return error("ROOM_SUBSCRIBE_NOT_FOUND", "채팅방 정보를 찾을 수 없습니다.", false);
        }

        if (!participant.getUser().getId().equals(sessionContext.userId())
                || participant.getLeftAt() != null) {
            return error("ROOM_SUBSCRIBE_FORBIDDEN", "해당 방 채널을 구독할 권한이 없습니다.", false);
        }

        return null;
    }

    private Long validateUnsubscribeRoomRequest(
            SocketRoomUnsubscribeRequest request, SocketSessionContext sessionContext) {

        if (request == null || request.roomId() == null || request.participantId() == null) {
            return null;
        }

        ChatRoomParticipant participant = getParticipant(request.participantId());

        if (participant == null) {
            return null;
        }

        if (!participant.getChatRoom().getId().equals(request.roomId())) {
            return null;
        }

        if (!participant.getUser().getId().equals(sessionContext.userId())) {
            return null;
        }

        return participant.getChatRoom().getId();
    }

    private ChatRoomParticipant getParticipant(Long participantId) {
        return chatRoomParticipantService.findById(participantId).orElse(null);
    }

    private SocketEventResponse<SocketErrorResponse> error(
            String code, String message, boolean retryable) {
        return SocketEventResponse.of(
                SOCKET_ERROR_EVENT, SocketErrorResponse.of(code, message, retryable));
    }
}
