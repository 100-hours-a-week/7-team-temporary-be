package molip.server.friend.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.friend.entity.Friend;
import molip.server.friend.repository.FriendRepository;
import molip.server.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;

    @Transactional(readOnly = true)
    public boolean isAlreadyFriend(Long userId, Long friendUserId) {
        return friendRepository.existsByUserIdAndFriendIdAndDeletedAtIsNull(userId, friendUserId)
                || friendRepository.existsByUserIdAndFriendIdAndDeletedAtIsNull(
                        friendUserId, userId);
    }

    @Transactional
    public void createFriendRelations(Users requestedUser, Users sentUser) {
        friendRepository.save(new Friend(requestedUser, sentUser.getId()));
        friendRepository.save(new Friend(sentUser, requestedUser.getId()));
    }

    @Transactional
    public void deleteFriend(Long userId, Long friendUserId) {

        Friend mine =
                friendRepository
                        .findByUserIdAndFriendIdAndDeletedAtIsNull(userId, friendUserId)
                        .orElse(null);

        Friend opposite =
                friendRepository
                        .findByUserIdAndFriendIdAndDeletedAtIsNull(friendUserId, userId)
                        .orElse(null);

        if (mine == null && opposite == null) {
            throw new BaseException(ErrorCode.REQUEST_NOT_FOUND_FRIEND);
        }

        if (mine != null) {
            mine.deleteFriend();
        }

        if (opposite != null) {
            opposite.deleteFriend();
        }
    }

    @Transactional(readOnly = true)
    public Page<Friend> getFriends(Long userId, int page, int size) {
        validatePage(page, size);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        return friendRepository.findByUserIdAndDeletedAtIsNull(userId, pageRequest);
    }

    private void validatePage(int page, int size) {

        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }
}
