package molip.server.reflection.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.reflection.entity.DayReflection;
import molip.server.reflection.entity.ReflectionLike;
import molip.server.reflection.service.ReflectionLikeService;
import molip.server.reflection.service.ReflectionService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReflectionLikeCommandFacade {

    private final ReflectionLikeService reflectionLikeService;
    private final ReflectionService reflectionService;
    private final UserService userService;

    @Transactional
    public void likeReflection(Long userId, Long reflectionId) {
        validateLike(userId, reflectionId);

        DayReflection reflection = reflectionService.getReflection(reflectionId);

        Users user = userService.getUser(userId);

        boolean alreadyLiked = reflectionLikeService.isAlreadyLiked(userId, reflectionId);

        if (alreadyLiked) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_LIKED);
        }

        reflectionLikeService.save(user, reflection);
    }

    @Transactional
    public void unlikeReflection(Long userId, Long reflectionId) {
        validateLike(userId, reflectionId);

        DayReflection existingReflection = reflectionService.getReflection(reflectionId);
        Users existingUser = userService.getUser(userId);

        ReflectionLike like = reflectionLikeService.getLike(userId, reflectionId);

        like.delete();
    }

    private void validateLike(Long userId, Long reflectionId) {
        if (userId == null || reflectionId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }
}
