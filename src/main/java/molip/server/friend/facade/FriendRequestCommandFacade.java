package molip.server.friend.facade;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.FriendRequestStatus;
import molip.server.friend.dto.response.FriendRequestResponse;
import molip.server.friend.entity.FriendRequest;
import molip.server.friend.service.FriendRequestService;
import molip.server.user.entity.Users;
import molip.server.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FriendRequestCommandFacade {

    private final FriendRequestService friendRequestService;
    private final UserService userService;

    @Transactional
    public FriendRequestResponse sendFriendRequest(Long fromUserId, Long targetUserId) {
        Users toUser = userService.getUser(targetUserId);
        FriendRequest saved = friendRequestService.sendFriendRequest(fromUserId, toUser);

        return FriendRequestResponse.from(saved.getId());
    }

    @Transactional
    public void deleteFriendRequest(Long userId, Long requestId) {

        friendRequestService.deleteFriendRequest(userId, requestId);
    }

    @Transactional
    public void updateFriendRequestStatus(Long userId, Long requestId, FriendRequestStatus status) {

        friendRequestService.acceptFriendRequest(userId, requestId, status);
    }
}
