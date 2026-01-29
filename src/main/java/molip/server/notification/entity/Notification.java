package molip.server.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.common.enums.NotificationStatus;
import molip.server.common.enums.NotificationType;
import molip.server.user.entity.Users;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    public Notification(
            Users user,
            NotificationType type,
            String title,
            String content,
            NotificationStatus status,
            LocalDateTime scheduledAt) {

        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.status = status;
        this.scheduledAt = scheduledAt;
    }

    public void markSent(LocalDateTime sentAt) {

        this.status = NotificationStatus.SENT;
        this.sentAt = sentAt;
    }

    public void markFailed() {

        this.status = NotificationStatus.FAILED;
    }

    public void deleteNotification() {

        this.deletedAt = LocalDateTime.now();
    }
}
