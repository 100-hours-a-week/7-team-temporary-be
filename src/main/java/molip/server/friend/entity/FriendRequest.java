package molip.server.friend.entity;

import jakarta.persistence.Column;
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
import molip.server.common.enums.FriendRequestStatus;
import molip.server.user.entity.Users;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FriendRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private Users toUser;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status;

    private LocalDateTime respondedAt;

    public FriendRequest(Long fromUserId, Users toUser) {
        this.fromUserId = fromUserId;
        this.toUser = toUser;
        this.status = FriendRequestStatus.PENDING;
    }

    public void deleteRequest() {
        this.deletedAt = LocalDateTime.now();
    }
}
