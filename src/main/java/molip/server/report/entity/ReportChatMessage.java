package molip.server.report.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "report_chat_message")
public class ReportChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    private SenderType senderType;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String content;

    private boolean isDeleted;

    private LocalDateTime sentAt;

    public ReportChatMessage(
            Report report,
            SenderType senderType,
            MessageType messageType,
            String content,
            boolean isDeleted,
            LocalDateTime sentAt) {

        this.report = report;
        this.senderType = senderType;
        this.messageType = messageType;
        this.content = content;
        this.isDeleted = isDeleted;
        this.sentAt = sentAt;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
