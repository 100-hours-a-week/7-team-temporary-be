package molip.server.chat.facade;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.response.WebRtcTokenIssueResponse;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.chat.service.video.WebRtcToken;
import molip.server.chat.service.video.WebRtcTokenIssueService;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WebRtcCommandFacade {

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final WebRtcTokenIssueService webRtcTokenIssueService;

    @Transactional(readOnly = true)
    public WebRtcTokenIssueResponse issueToken(Long loginUserId, Long roomId, Long participantId) {
        validateRequired(loginUserId, roomId, participantId);

        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);

        if (chatRoom.getType() != ChatRoomType.CAM_STUDY) {
            throw new BaseException(ErrorCode.VIDEO_ROOM_REQUIRED);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantService.getActiveParticipantByIdAndRoomId(participantId, roomId);

        if (!loginUserId.equals(participant.getUser().getId())) {
            throw new BaseException(ErrorCode.VIDEO_PARTICIPANT_FORBIDDEN);
        }

        WebRtcToken issuedToken =
                webRtcTokenIssueService.issue(
                        roomId, participantId, loginUserId, participant.getUser().getNickname());

        return WebRtcTokenIssueResponse.of(
                roomId,
                participantId,
                issuedToken.webrtcRoomName(),
                issuedToken.accessToken(),
                issuedToken.expiresAt());
    }

    private void validateRequired(Long loginUserId, Long roomId, Long participantId) {
        if (loginUserId == null || roomId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }
}
