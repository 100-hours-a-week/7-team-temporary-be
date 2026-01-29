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
import molip.server.common.enums.Platform;
import molip.server.user.entity.Users;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    private Boolean isActive;

    private LocalDateTime lastSeenAt;

    public UserFcmToken(
            Users user,
            String fcmToken,
            Platform platform,
            Boolean isActive,
            LocalDateTime lastSeenAt) {

        this.user = user;
        this.fcmToken = fcmToken;
        this.platform = platform;
        this.isActive = isActive;
        this.lastSeenAt = lastSeenAt;
    }

    public void deactivate() {

        this.isActive = false;
    }

    public void updateLastSeen(LocalDateTime lastSeenAt) {

        this.lastSeenAt = lastSeenAt;
    }
}
