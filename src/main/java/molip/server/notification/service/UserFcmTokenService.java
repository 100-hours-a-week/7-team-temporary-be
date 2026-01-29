package molip.server.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.notification.repository.UserFcmTokenRepository;
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
}
