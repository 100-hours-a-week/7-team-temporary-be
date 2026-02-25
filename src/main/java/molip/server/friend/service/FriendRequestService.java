package molip.server.friend.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FriendRequestStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.friend.entity.Friend;
import molip.server.friend.entity.FriendRequest;
import molip.server.friend.repository.FriendRepository;
import molip.server.friend.repository.FriendRequestRepository;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public FriendRequest sendFriendRequest(Long fromUserId, Users toUser) {
        validateSelfRequest(fromUserId, toUser.getId());
        validateAlreadyFriend(fromUserId, toUser.getId());
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

    @Transactional
    public void deleteFriendRequest(Long userId, Long requestId) {
        FriendRequest request =
                friendRequestRepository
                        .findByIdAndDeletedAtIsNull(requestId)
                        .orElseThrow(() -> new BaseException(ErrorCode.REQUEST_NOT_FOUND));

        validateRequestReceiver(userId, request);
        validateRequestPending(request);

        request.reject();
    }

    @Transactional
    public void acceptFriendRequest(Long userId, Long requestId, FriendRequestStatus status) {
        validateAcceptStatus(status);

        FriendRequest request =
                friendRequestRepository
                        .findByIdAndDeletedAtIsNull(requestId)
                        .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_REQUEST));

        validateAcceptReceiver(userId, request);
        validateRequestPending(request);
        validateAlreadyFriend(userId, request.getFromUserId());

        createFriendRelations(userId, request.getFromUserId());

        request.accept();
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

    private void validateRequestReceiver(Long userId, FriendRequest request) {

        if (!request.getToUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_DELETE_REQUEST);
        }
    }

    private void validateRequestPending(FriendRequest request) {

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_HANDLED_REQUEST);
        }
    }

    private void validateAcceptStatus(FriendRequestStatus status) {

        if (status != FriendRequestStatus.ACCEPTED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_STATUS_VALUE);
        }
    }

    private void validateAcceptReceiver(Long userId, FriendRequest request) {

        if (!request.getToUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_ACCEPT_REQUEST);
        }
    }

    private void validateAlreadyFriend(Long userId, Long fromUserId) {

        if (friendRepository.existsByUserIdAndFriendIdAndDeletedAtIsNull(userId, fromUserId)
                || friendRepository.existsByUserIdAndFriendIdAndDeletedAtIsNull(
                        fromUserId, userId)) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_FRIEND);
        }
    }

    private void createFriendRelations(Long toUserId, Long fromUserId) {
        Users receiver =
                userRepository
                        .findByIdAndDeletedAtIsNull(toUserId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        Users sender =
                userRepository
                        .findByIdAndDeletedAtIsNull(fromUserId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND_TARGET));

        friendRepository.save(new Friend(receiver, fromUserId));
        friendRepository.save(new Friend(sender, toUserId));
    }
}
