package molip.server.friend.repository;

import java.util.Collection;
import java.util.List;
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

    @Query(
            "select fr.toUser.id "
                    + "from FriendRequest fr "
                    + "where fr.fromUserId = :userId "
                    + "and fr.toUser.id in :searchedUserIds "
                    + "and fr.status = :status "
                    + "and fr.deletedAt is null")
    List<Long> findFriendRequestsSentByMe(
            @Param("userId") Long userId,
            @Param("searchedUserIds") Collection<Long> searchedUserIds,
            @Param("status") FriendRequestStatus status);

    @Query(
            "select fr.fromUserId "
                    + "from FriendRequest fr "
                    + "where fr.toUser.id = :userId "
                    + "and fr.fromUserId in :searchedUserIds "
                    + "and fr.status = :status "
                    + "and fr.deletedAt is null")
    List<Long> findFriendRequestsSentByOthers(
            @Param("userId") Long userId,
            @Param("searchedUserIds") Collection<Long> searchedUserIds,
            @Param("status") FriendRequestStatus status);
}
