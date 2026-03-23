package molip.server.socket.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.redis.presence.RedisChatParticipantPresenceStore;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.socket.dto.request.SocketChatParticipantHeartbeatRequest;
import molip.server.socket.dto.request.SocketChatParticipantOfflineRequest;
import molip.server.socket.dto.request.SocketChatParticipantOnlineRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketRoomChatPresenceService {

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final RedisChatParticipantPresenceStore redisChatParticipantPresenceStore;

    public void markOnline(Long userId, SocketChatParticipantOnlineRequest request) {
        if (request == null
                || request.roomId() == null
                || request.participantId() == null
                || request.sessionId() == null
                || request.sessionId().isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ChatRoomParticipant participant =
                getAuthorizedParticipant(userId, request.roomId(), request.participantId());
        redisChatParticipantPresenceStore.upsertOnline(
                request.roomId(),
                participant.getId(),
                participant.getUser().getId(),
                request.sessionId().trim(),
                OffsetDateTime.now(ZoneOffset.of("+09:00")));
    }

    public void heartbeat(Long userId, SocketChatParticipantHeartbeatRequest request) {
        if (request == null
                || request.roomId() == null
                || request.participantId() == null
                || request.sessionId() == null
                || request.sessionId().isBlank()) {
            return;
        }

        ChatRoomParticipant participant =
                getAuthorizedParticipant(userId, request.roomId(), request.participantId());
        redisChatParticipantPresenceStore.touchHeartbeat(
                request.roomId(),
                participant.getId(),
                request.sessionId().trim(),
                OffsetDateTime.now(ZoneOffset.of("+09:00")));
    }

    public void markOffline(Long userId, SocketChatParticipantOfflineRequest request) {
        if (request == null
                || request.roomId() == null
                || request.participantId() == null
                || request.sessionId() == null
                || request.sessionId().isBlank()) {
            return;
        }

        ChatRoomParticipant participant =
                getAuthorizedParticipant(userId, request.roomId(), request.participantId());
        redisChatParticipantPresenceStore.removeOffline(
                request.roomId(), participant.getId(), request.sessionId().trim());
    }

    private ChatRoomParticipant getAuthorizedParticipant(
            Long userId, Long roomId, Long participantId) {
        if (userId == null || roomId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantService.getActiveParticipantByIdAndRoomId(participantId, roomId);

        if (!participant.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_PARTICIPANT_REMOVE);
        }

        return participant;
    }
}
