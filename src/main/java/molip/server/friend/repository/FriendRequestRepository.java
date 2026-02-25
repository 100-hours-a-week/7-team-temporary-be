package molip.server.friend.repository;

import molip.server.common.enums.FriendRequestStatus;
import molip.server.friend.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsByFromUserIdAndToUserIdAndStatusAndDeletedAtIsNull(
            Long fromUserId, Long toUserId, FriendRequestStatus status);
}
