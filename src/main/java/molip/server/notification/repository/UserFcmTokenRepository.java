package molip.server.notification.repository;

import java.util.List;
import molip.server.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    @Query(
            "select u.fcmToken from UserFcmToken u "
                    + "where u.user.id = :userId "
                    + "and u.isActive = true "
                    + "and u.deletedAt is null")
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);
}
