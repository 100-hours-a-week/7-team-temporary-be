package molip.server.friend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import molip.server.friend.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserIdAndFriendIdAndDeletedAtIsNull(Long userId, Long friendId);

    Optional<Friend> findByUserIdAndFriendIdAndDeletedAtIsNull(Long userId, Long friendId);

    Page<Friend> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query(
            "select f.friendId "
                    + "from Friend f "
                    + "where f.user.id = :userId "
                    + "and f.friendId in :searchedUserIds "
                    + "and f.deletedAt is null")
    List<Long> findFriendIdsAcceptedByMe(
            @Param("userId") Long userId,
            @Param("searchedUserIds") Collection<Long> searchedUserIds);

    @Query(
            "select f.user.id "
                    + "from Friend f "
                    + "where f.user.id in :searchedUserIds "
                    + "and f.friendId = :userId "
                    + "and f.deletedAt is null")
    List<Long> findFriendIdsAcceptedByOthers(
            @Param("searchedUserIds") Collection<Long> searchedUserIds,
            @Param("userId") Long userId);
}
