package molip.server.reflection.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.service.ReflectionLikeService;
import molip.server.reflection.service.ReflectionService;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReflectionLikeCommandFacade {

    private final ReflectionLikeService reflectionLikeService;
    private final ReflectionService reflectionService;
    private final UserRepository userRepository;

    @Transactional
    public void likeReflection(Long userId, Long reflectionId) {
        validateLike(userId, reflectionId);

        DayReflection reflection = reflectionService.getReflection(reflectionId);

        Users user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean alreadyLiked = reflectionLikeService.isAlreadyLiked(userId, reflectionId);

        if (alreadyLiked) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_LIKED);
        }

        reflectionLikeService.save(user, reflection);
    }

    private void validateLike(Long userId, Long reflectionId) {
        if (userId == null || reflectionId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }
}
