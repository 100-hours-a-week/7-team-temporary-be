package molip.server.notification.facade;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.Platform;
import molip.server.notification.service.UserFcmTokenService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFcmTokenCommandFacade {

    private final UserService userService;
    private final UserFcmTokenService userFcmTokenService;

    @Transactional
    public void upsertToken(Long userId, String fcmToken, Platform platform) {

        Users user = userService.getUser(userId);

        userFcmTokenService.upsertToken(user, fcmToken, platform, LocalDateTime.now());
    }

    @Transactional
    public void deactivateToken(Long userId, String fcmToken) {

        userFcmTokenService.deactivateToken(userId, fcmToken, LocalDateTime.now());
    }
}
