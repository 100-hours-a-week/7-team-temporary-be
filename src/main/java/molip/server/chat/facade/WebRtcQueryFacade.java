package molip.server.chat.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.VideoOnlineParticipantItemResponse;
import molip.server.chat.dto.response.VideoOnlineParticipantsResponse;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.redis.presence.RedisVideoParticipantPresenceStore;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebRtcQueryFacade {

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final RedisVideoParticipantPresenceStore redisVideoParticipantPresenceStore;

    @Transactional(readOnly = true)
    public VideoOnlineParticipantsResponse getOnlineParticipants(Long loginUserId, Long roomId) {
        ChatRoom room = chatRoomService.getChatRoom(roomId);
        if (room.getType() != ChatRoomType.CAM_STUDY) {
            throw new BaseException(ErrorCode.VIDEO_ROOM_REQUIRED);
        }

        chatRoomParticipantService
                .getActiveParticipant(roomId, loginUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.FORBIDDEN_CHAT_ACCESS));

        List<VideoOnlineParticipantItemResponse> participants =
                redisVideoParticipantPresenceStore.findOnlineByRoomId(roomId).stream()
                        .map(
                                state ->
                                        VideoOnlineParticipantItemResponse.of(
                                                state.participantId(),
                                                state.userId(),
                                                state.sessionId(),
                                                state.cameraEnabled(),
                                                state.onlineAt(),
                                                state.lastHeartbeatAt()))
                        .toList();

        return VideoOnlineParticipantsResponse.of(roomId, participants.size(), participants);
    }
}
