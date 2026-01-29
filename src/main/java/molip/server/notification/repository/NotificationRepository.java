package molip.server.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import molip.server.common.enums.NotificationStatus;
import molip.server.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(
            "select n from Notification n "
                    + "where n.status = :status "
                    + "and n.scheduledAt <= :now "
                    + "and n.deletedAt is null")
    List<Notification> findPendingNotifications(
            @Param("status") NotificationStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
