package molip.server.chat.entity;

import jakarta.persistence.Entity;
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
import molip.server.user.entity.Users;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatRoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    private Long lastSeenMessageId;

    private boolean cameraEnabled;

    private LocalDateTime leftAt;

    public ChatRoomParticipant(
            Users user, ChatRoom chatRoom, Long lastSeenMessageId, boolean cameraEnabled) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.lastSeenMessageId = lastSeenMessageId;
        this.cameraEnabled = cameraEnabled;
    }

    public void updateLastSeenMessageId(Long lastSeenMessageId) {
        this.lastSeenMessageId = lastSeenMessageId;
    }

    public void updateCameraEnabled(boolean cameraEnabled) {
        this.cameraEnabled = cameraEnabled;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }
}
