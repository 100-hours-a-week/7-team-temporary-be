package molip.server.chat.facade;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.VideoParticipantPresenceResponse;
import molip.server.chat.dto.response.VideoPublishChangedResponse;
import molip.server.chat.dto.response.VideoSessionSyncedResponse;
import molip.server.chat.dto.response.VideoTokenIssuedResponse;
import molip.server.chat.dto.response.WebRtcTokenIssueResponse;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.VideoParticipantOfflineEvent;
import molip.server.chat.event.VideoParticipantOnlineEvent;
import molip.server.chat.event.VideoSessionAcknowledgedEvent;
import molip.server.chat.event.VideoSessionSyncedEvent;
import molip.server.chat.event.VideoTokenIssuedEvent;
import molip.server.chat.redis.presence.RedisVideoParticipantPresenceStore;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.chat.service.ChatRoomService;
import molip.server.chat.service.video.WebRtcToken;
import molip.server.chat.service.video.WebRtcTokenIssueService;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebRtcCommandFacade {

    private static final String EVENT_VIDEO_PUBLISH_STARTED = "video.publish.started";
    private static final String EVENT_VIDEO_PUBLISH_STOPPED = "video.publish.stopped";

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final WebRtcTokenIssueService webRtcTokenIssueService;
    private final RedisVideoParticipantPresenceStore redisVideoParticipantPresenceStore;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public WebRtcTokenIssueResponse issueToken(Long loginUserId, Long roomId, Long participantId) {
        validateRequired(loginUserId, roomId, participantId);
        log.info(
                "livekit token issue requested: roomId={}, participantId={}, loginUserId={}",
                roomId,
                participantId,
                loginUserId);

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
        log.info(
                "livekit token issued: roomId={}, participantId={}, expiresAt={}",
                roomId,
                participantId,
                issuedToken.expiresAt());

        eventPublisher.publishEvent(
                new VideoTokenIssuedEvent(
                        loginUserId,
                        VideoTokenIssuedResponse.of(
                                roomId,
                                participantId,
                                issuedToken.expiresAt(),
                                OffsetDateTime.now(ZoneOffset.of("+09:00")))));

        return WebRtcTokenIssueResponse.of(
                roomId,
                participantId,
                issuedToken.webrtcRoomName(),
                issuedToken.accessToken(),
                issuedToken.expiresAt());
    }

    @Transactional
    public void syncVideoSession(
            Long loginUserId,
            Long roomId,
            Long participantId,
            String sessionId,
            Boolean published) {
        validateRequired(loginUserId, roomId, participantId);
        log.info(
                "livekit session sync requested: roomId={}, participantId={}, published={}, sessionId={}",
                roomId,
                participantId,
                published,
                sessionId);

        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
        if (chatRoom.getType() != ChatRoomType.CAM_STUDY) {
            throw new BaseException(ErrorCode.VIDEO_ROOM_REQUIRED);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantService.getActiveParticipantByIdAndRoomId(participantId, roomId);

        if (!loginUserId.equals(participant.getUser().getId())) {
            throw new BaseException(ErrorCode.VIDEO_PARTICIPANT_FORBIDDEN);
        }

        String eventType = resolveVideoPublishEventType(published);

        VideoPublishChangedResponse payload =
                buildVideoPublishChangedPayload(roomId, participant, sessionId, published);

        eventPublisher.publishEvent(new VideoSessionSyncedEvent(roomId, eventType, payload));
        eventPublisher.publishEvent(
                new VideoSessionAcknowledgedEvent(
                        roomId,
                        VideoSessionSyncedResponse.of(
                                roomId,
                                participantId,
                                sessionId.trim(),
                                published,
                                OffsetDateTime.now(ZoneOffset.of("+09:00")))));
        log.info(
                "livekit session sync published: roomId={}, participantId={}, eventType={}",
                roomId,
                participantId,
                eventType);
    }

    @Transactional
    public void markParticipantOnline(
            Long loginUserId,
            Long roomId,
            Long participantId,
            String sessionId,
            Boolean cameraEnabled) {
        validateRequired(loginUserId, roomId, participantId);
        if (cameraEnabled == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ChatRoomParticipant participant =
                getAuthorizedCamStudyParticipant(loginUserId, roomId, participantId);
        String normalizedSessionId = normalizeSessionId(sessionId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+09:00"));

        redisVideoParticipantPresenceStore.upsertOnline(
                roomId,
                participant.getId(),
                participant.getUser().getId(),
                participant.getUser().getNickname(),
                normalizedSessionId,
                cameraEnabled,
                now);

        eventPublisher.publishEvent(
                new VideoParticipantOnlineEvent(
                        roomId,
                        VideoParticipantPresenceResponse.of(
                                UUID.randomUUID().toString(),
                                roomId,
                                participant.getId(),
                                participant.getUser().getId(),
                                participant.getUser().getNickname(),
                                normalizedSessionId,
                                cameraEnabled,
                                now)));
    }

    @Transactional
    public void heartbeatParticipant(
            Long loginUserId, Long roomId, Long participantId, String sessionId) {
        validateRequired(loginUserId, roomId, participantId);

        ChatRoomParticipant participant =
                getAuthorizedCamStudyParticipant(loginUserId, roomId, participantId);
        redisVideoParticipantPresenceStore.touchHeartbeat(
                roomId,
                participant.getId(),
                normalizeSessionId(sessionId),
                OffsetDateTime.now(ZoneOffset.of("+09:00")));
    }

    @Transactional
    public void markParticipantOffline(
            Long loginUserId, Long roomId, Long participantId, String sessionId) {
        validateRequired(loginUserId, roomId, participantId);

        ChatRoomParticipant participant =
                getAuthorizedCamStudyParticipant(loginUserId, roomId, participantId);
        String normalizedSessionId = normalizeSessionId(sessionId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+09:00"));

        redisVideoParticipantPresenceStore.removeOffline(
                roomId, participant.getId(), normalizedSessionId);

        eventPublisher.publishEvent(
                new VideoParticipantOfflineEvent(
                        roomId,
                        VideoParticipantPresenceResponse.of(
                                UUID.randomUUID().toString(),
                                roomId,
                                participant.getId(),
                                participant.getUser().getId(),
                                participant.getUser().getNickname(),
                                normalizedSessionId,
                                participant.isCameraEnabled(),
                                now)));
    }

    private void validateRequired(Long loginUserId, Long roomId, Long participantId) {
        if (loginUserId == null || roomId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private String resolveVideoPublishEventType(Boolean published) {
        if (published == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        return published ? EVENT_VIDEO_PUBLISH_STARTED : EVENT_VIDEO_PUBLISH_STOPPED;
    }

    private ChatRoomParticipant getAuthorizedCamStudyParticipant(
            Long loginUserId, Long roomId, Long participantId) {
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
        if (chatRoom.getType() != ChatRoomType.CAM_STUDY) {
            throw new BaseException(ErrorCode.VIDEO_ROOM_REQUIRED);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantService.getActiveParticipantByIdAndRoomId(participantId, roomId);
        if (!loginUserId.equals(participant.getUser().getId())) {
            throw new BaseException(ErrorCode.VIDEO_PARTICIPANT_FORBIDDEN);
        }

        return participant;
    }

    private String normalizeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        return sessionId.trim();
    }

    private VideoPublishChangedResponse buildVideoPublishChangedPayload(
            Long roomId, ChatRoomParticipant participant, String sessionId, Boolean published) {
        if (participant == null || published == null || sessionId == null || sessionId.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        return VideoPublishChangedResponse.of(
                UUID.randomUUID().toString(),
                roomId,
                participant.getId(),
                participant.getUser().getId(),
                sessionId.trim(),
                OffsetDateTime.now(ZoneOffset.of("+09:00")));
    }
}
