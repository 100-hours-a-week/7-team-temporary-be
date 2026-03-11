package molip.server.chat.service.video;

import lombok.RequiredArgsConstructor;
import molip.server.chat.config.LiveKitProperties;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebRtcTokenIssueService {

    private final LiveKitTokenPort liveKitTokenPort;
    private final LiveKitProperties liveKitProperties;

    public WebRtcToken issue(Long roomId, Long participantId, Long userId, String nickname) {
        validate(roomId, participantId, userId);

        String roomName = toRoomName(roomId);
        String identity = toIdentity(userId, participantId);
        long ttlSeconds = resolveTtlSeconds();

        IssuedLiveKitToken issuedToken =
                liveKitTokenPort.issueToken(
                        LiveKitTokenCommand.of(
                                roomName, identity, nickname, true, true, ttlSeconds));

        if (issuedToken == null
                || issuedToken.accessToken() == null
                || issuedToken.accessToken().isBlank()
                || issuedToken.expiresAt() == null) {
            throw new BaseException(ErrorCode.VIDEO_TOKEN_ISSUE_FAILED);
        }

        return WebRtcToken.of(roomName, issuedToken.accessToken(), issuedToken.expiresAt());
    }

    private void validate(Long roomId, Long participantId, Long userId) {
        if (roomId == null || participantId == null || userId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private String toRoomName(Long roomId) {
        return "chat-room-" + roomId;
    }

    private String toIdentity(Long userId, Long participantId) {
        return "user:%d:participant:%d".formatted(userId, participantId);
    }

    private long resolveTtlSeconds() {
        long ttlSeconds = liveKitProperties.getTokenTtlSeconds();
        return ttlSeconds > 0 ? ttlSeconds : 900L;
    }
}
