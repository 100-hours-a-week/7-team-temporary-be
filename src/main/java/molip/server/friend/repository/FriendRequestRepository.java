package molip.server.friend.repository;

import java.util.Optional;
import molip.server.common.enums.FriendRequestStatus;
import molip.server.friend.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsByFromUserIdAndToUserIdAndStatusAndDeletedAtIsNull(
            Long fromUserId, Long toUserId, FriendRequestStatus status);

    Optional<FriendRequest> findByIdAndDeletedAtIsNull(Long requestId);

    @Query(
            "select fr "
                    + "from FriendRequest fr "
                    + "join Users u on u.id = fr.fromUserId "
                    + "where fr.toUser.id = :toUserId "
                    + "and fr.status = :status "
                    + "and fr.deletedAt is null "
                    + "and u.deletedAt is null "
                    + "order by fr.createdAt desc")
    Page<FriendRequest> findReceivedRequests(
            @Param("toUserId") Long toUserId,
            @Param("status") FriendRequestStatus status,
            Pageable pageable);
}
