package molip.server.socket.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.socket.dto.request.SocketVideoCameraToggleRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketVideoCameraToggleAcceptedResponse;
import molip.server.socket.dto.response.SocketVideoCameraToggleRejectedResponse;
import molip.server.socket.dto.response.SocketVideoErrorResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketRoomVideoService {

    private static final String EVENT_ACCEPTED = "video.camera.toggleAccepted";
    private static final String EVENT_REJECTED = "video.camera.toggleRejected";
    private static final String EVENT_VIDEO_ERROR = "video.error";

    private final ChatRoomCommandFacade chatRoomCommandFacade;
    private final SocketRoomChannelBroadcaster socketRoomChannelBroadcaster;

    public SocketEventResponse<?> toggleCamera(
            Long userId, String sessionId, SocketVideoCameraToggleRequest request) {
        if (userId == null
                || request == null
                || request.roomId() == null
                || request.participantId() == null
                || request.cameraEnabled() == null) {
            return rejected(
                    request == null ? null : request.roomId(),
                    request == null ? null : request.participantId(),
                    request == null ? null : request.requestId(),
                    "VIDEO_CAMERA_INVALID_PAYLOAD",
                    ErrorCode.INVALID_REQUEST_REQUIRED_VALUES.getMessage());
        }

        try {
            chatRoomCommandFacade.updateParticipantCamera(
                    userId, request.roomId(), request.participantId(), request.cameraEnabled());

            return SocketEventResponse.of(
                    EVENT_ACCEPTED,
                    SocketVideoCameraToggleAcceptedResponse.of(
                            request.requestId(),
                            request.roomId(),
                            request.participantId(),
                            request.cameraEnabled(),
                            "SUCCEEDED",
                            OffsetDateTime.now(ZoneOffset.of("+09:00"))));

        } catch (BaseException exception) {
            return handleBusinessError(
                    sessionId,
                    request.roomId(),
                    request.participantId(),
                    request.requestId(),
                    exception.getErrorCode());
        } catch (Exception exception) {
            publishVideoError(
                    sessionId,
                    request.roomId(),
                    request.participantId(),
                    "VIDEO_INTERNAL_ERROR",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                    true);

            return rejected(
                    request.roomId(),
                    request.participantId(),
                    request.requestId(),
                    "VIDEO_INTERNAL_ERROR",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private SocketEventResponse<SocketVideoCameraToggleRejectedResponse> handleBusinessError(
            String sessionId,
            Long roomId,
            Long participantId,
            String requestId,
            ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST_CAMERA_REQUIRED, INVALID_REQUEST_REQUIRED_VALUES ->
                    rejected(
                            roomId,
                            participantId,
                            requestId,
                            "VIDEO_CAMERA_INVALID_PAYLOAD",
                            errorCode.getMessage());
            case FORBIDDEN_CAMERA_UPDATE, VIDEO_PARTICIPANT_FORBIDDEN ->
                    rejected(
                            roomId,
                            participantId,
                            requestId,
                            "VIDEO_CAMERA_FORBIDDEN",
                            errorCode.getMessage());
            case PARTICIPANT_NOT_FOUND ->
                    rejected(
                            roomId,
                            participantId,
                            requestId,
                            "VIDEO_CAMERA_PARTICIPANT_NOT_FOUND",
                            errorCode.getMessage());
            default -> {
                publishVideoError(
                        sessionId,
                        roomId,
                        participantId,
                        "VIDEO_INTERNAL_ERROR",
                        errorCode.getMessage(),
                        true);
                yield rejected(
                        roomId,
                        participantId,
                        requestId,
                        "VIDEO_INTERNAL_ERROR",
                        errorCode.getMessage());
            }
        };
    }

    private SocketEventResponse<SocketVideoCameraToggleRejectedResponse> rejected(
            Long roomId, Long participantId, String requestId, String code, String message) {
        return rejected(roomId, participantId, requestId, code, message, false);
    }

    private SocketEventResponse<SocketVideoCameraToggleRejectedResponse> rejected(
            Long roomId,
            Long participantId,
            String requestId,
            String code,
            String message,
            boolean retryable) {
        return SocketEventResponse.of(
                EVENT_REJECTED,
                SocketVideoCameraToggleRejectedResponse.of(
                        roomId, participantId, requestId, code, message, retryable));
    }

    private void publishVideoError(
            String sessionId,
            Long roomId,
            Long participantId,
            String code,
            String message,
            boolean retryable) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        socketRoomChannelBroadcaster.sendToSession(
                sessionId,
                EVENT_VIDEO_ERROR,
                SocketVideoErrorResponse.of(
                        roomId,
                        participantId,
                        code,
                        message,
                        retryable,
                        OffsetDateTime.now(ZoneOffset.of("+09:00"))));
    }
}
