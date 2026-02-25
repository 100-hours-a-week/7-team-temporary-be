package molip.server.friend.repository;

import java.util.Optional;
import molip.server.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserIdAndFriendIdAndDeletedAtIsNull(Long userId, Long friendId);

    Optional<Friend> findByUserIdAndFriendIdAndDeletedAtIsNull(Long userId, Long friendId);
}
