package molip.server.chat.entity;

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
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String content;

    private boolean isDeleted;

    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private SenderType senderType;

    private Long senderId;

    public ChatMessage(
            ChatRoom chatRoom,
            MessageType messageType,
            String content,
            boolean isDeleted,
            LocalDateTime sentAt,
            SenderType senderType,
            Long senderId) {
        this.chatRoom = chatRoom;
        this.messageType = messageType;
        this.content = content;
        this.isDeleted = isDeleted;
        this.sentAt = sentAt;
        this.senderType = senderType;
        this.senderId = senderId;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
