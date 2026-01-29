package molip.server.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.Platform;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.notification.entity.UserFcmToken;
import molip.server.notification.repository.UserFcmTokenRepository;
import molip.server.user.entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFcmTokenService {

    private final UserFcmTokenRepository userFcmTokenRepository;

    @Transactional(readOnly = true)
    public List<String> getActiveTokens(Long userId) {

        return userFcmTokenRepository.findActiveTokensByUserId(userId);
    }

    @Transactional
    public void upsertToken(
            Users user, String fcmToken, Platform platform, LocalDateTime lastSeenAt) {

        if (user == null || fcmToken == null || platform == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        userFcmTokenRepository
                .findByUserIdAndFcmTokenAndDeletedAtIsNull(user.getId(), fcmToken)
                .ifPresentOrElse(
                        token -> token.activate(lastSeenAt, platform),
                        () ->
                                userFcmTokenRepository.save(
                                        new UserFcmToken(
                                                user, fcmToken, platform, true, lastSeenAt)));
    }

    @Transactional
    public void deactivateToken(Long userId, String fcmToken, LocalDateTime lastSeenAt) {

        if (userId == null || fcmToken == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        UserFcmToken token =
                userFcmTokenRepository
                        .findByUserIdAndFcmTokenAndDeletedAtIsNull(userId, fcmToken)
                        .orElseThrow(() -> new BaseException(ErrorCode.FCM_TOKEN_NOT_FOUND));

        token.deactivate();
        token.updateLastSeen(lastSeenAt);
    }
}
