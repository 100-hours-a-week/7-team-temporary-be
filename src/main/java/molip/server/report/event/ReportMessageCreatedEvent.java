package molip.server.report.event;

import java.time.LocalDateTime;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record ReportMessageCreatedEvent(
        Long userId,
        Long reportId,
        Long messageId,
        SenderType senderType,
        MessageType messageType,
        String content,
        LocalDateTime sentAt) {}
