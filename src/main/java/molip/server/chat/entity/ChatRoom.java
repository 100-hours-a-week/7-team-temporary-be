package molip.server.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.common.enums.ChatRoomType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;

    private String title;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    private String description;

    private int maxParticipants;

    public ChatRoom(Long ownerId, String title, String description, int maxParticipants) {
        this.ownerId = ownerId;
        this.title = title;
        this.type = ChatRoomType.OPEN_CHAT;
        this.description = description;
        this.maxParticipants = maxParticipants;
    }

    public void updateRoom(String title, String description, int maxParticipants) {
        this.title = title;
        this.description = description;
        this.maxParticipants = maxParticipants;
    }

    public void deleteRoom() {
        this.deletedAt = LocalDateTime.now();
    }
}
