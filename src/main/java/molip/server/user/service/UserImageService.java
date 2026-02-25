package molip.server.user.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.user.entity.UserImage;
import molip.server.user.repository.UserImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;

    @Transactional(readOnly = true)
    public Optional<UserImage> getLatestUserImage(Long userId) {
        return userImageRepository.findLatestByUserIdWithImage(userId);
    }
}
