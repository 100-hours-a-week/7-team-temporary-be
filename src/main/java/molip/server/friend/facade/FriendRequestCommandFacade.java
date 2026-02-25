package molip.server.friend.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FriendRequestStatus;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.friend.dto.response.FriendRequestResponse;
import molip.server.friend.entity.FriendRequest;
import molip.server.friend.service.FriendRequestService;
import molip.server.friend.service.FriendService;
import molip.server.notification.event.FriendCreatedEvent;
import molip.server.notification.event.FriendRequestedEvent;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FriendRequestCommandFacade {

    private final FriendService friendService;
    private final FriendRequestService friendRequestService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FriendRequestResponse sendFriendRequest(Long fromUserId, Long targetUserId) {
        if (friendService.isAlreadyFriend(fromUserId, targetUserId)) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_FRIEND);
        }

        Users fromUser = userService.getUser(fromUserId);
        Users toUser = userService.getUser(targetUserId);
        FriendRequest saved = friendRequestService.sendFriendRequest(fromUserId, toUser);
        eventPublisher.publishEvent(
                new FriendRequestedEvent(toUser.getId(), fromUser.getNickname()));

        return FriendRequestResponse.from(saved.getId());
    }

    @Transactional
    public void deleteFriendRequest(Long userId, Long requestId) {

        friendRequestService.deleteFriendRequest(userId, requestId);
    }

    @Transactional
    public void updateFriendRequestStatus(Long userId, Long requestId, FriendRequestStatus status) {
        FriendRequest request = friendRequestService.acceptFriendRequest(userId, requestId, status);

        if (friendService.isAlreadyFriend(userId, request.getFromUserId())) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_FRIEND);
        }

        Users requestedUser = userService.getUser(userId);
        Users sentUser = userService.getUser(request.getFromUserId());

        friendService.createFriendRelations(requestedUser, sentUser);
        eventPublisher.publishEvent(
                new FriendCreatedEvent(sentUser.getId(), requestedUser.getNickname()));
    }
}
