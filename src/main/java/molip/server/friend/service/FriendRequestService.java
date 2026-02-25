package molip.server.friend.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FriendRequestStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.friend.entity.FriendRequest;
import molip.server.friend.repository.FriendRequestRepository;
import molip.server.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;

    @Transactional
    public FriendRequest sendFriendRequest(Long fromUserId, Users toUser) {
        validateSelfRequest(fromUserId, toUser.getId());
        validateDuplicatedRequest(fromUserId, toUser.getId());
        validateOppositeRequestExists(fromUserId, toUser.getId());

        return friendRequestRepository.save(new FriendRequest(fromUserId, toUser));
    }

    @Transactional(readOnly = true)
    public Page<FriendRequest> getReceivedFriendRequests(Long userId, int page, int size) {
        validatePage(page, size);

        PageRequest pageRequest = PageRequest.of(page - 1, size);

        return friendRequestRepository.findReceivedRequests(
                userId, FriendRequestStatus.PENDING, pageRequest);
    }

    private void validateSelfRequest(Long fromUserId, Long toUserId) {

        if (fromUserId.equals(toUserId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_SELF_FRIEND);
        }
    }

    private void validateDuplicatedRequest(Long fromUserId, Long toUserId) {

        if (friendRequestRepository.existsByFromUserIdAndToUserIdAndStatusAndDeletedAtIsNull(
                fromUserId, toUserId, FriendRequestStatus.PENDING)) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_REQUESTED);
        }
    }

    private void validateOppositeRequestExists(Long fromUserId, Long toUserId) {

        if (friendRequestRepository.existsByFromUserIdAndToUserIdAndStatusAndDeletedAtIsNull(
                toUserId, fromUserId, FriendRequestStatus.PENDING)) {
            throw new BaseException(ErrorCode.CONFLICT_RECEIVED_ALREADY);
        }
    }

    private void validatePage(int page, int size) {

        if (page < 1 || size < 1) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }
}
