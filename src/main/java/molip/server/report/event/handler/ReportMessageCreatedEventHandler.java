package molip.server.report.event.handler;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import molip.server.auth.store.redis.RedisDeviceStore;
import molip.server.report.event.ReportMessageCreatedEvent;
import molip.server.socket.dto.response.SocketReportMessageCreatedResponse;
import molip.server.socket.service.SocketReportChannelBroadcaster;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportMessageCreatedEventHandler {

    private static final String EVENT_CREATED = "report.message.created";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final RedisDeviceStore deviceStore;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketReportChannelBroadcaster socketReportChannelBroadcaster;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReportMessageCreatedEvent event) {
        Set<String> deviceIds = deviceStore.listDevices(event.userId());

        OffsetDateTime sentAt =
                OffsetDateTime.of(
                        event.sentAt(), KOREA_ZONE_ID.getRules().getOffset(event.sentAt()));

        for (String deviceId : deviceIds) {
            String sessionId = socketSessionStore.findSessionId(event.userId(), deviceId);

            if (sessionId == null || sessionId.isBlank()) {
                continue;
            }

            socketReportChannelBroadcaster.sendToSession(
                    sessionId,
                    EVENT_CREATED,
                    SocketReportMessageCreatedResponse.of(
                            UUID.randomUUID().toString(),
                            event.reportId(),
                            event.messageId(),
                            event.senderType(),
                            event.messageType(),
                            event.content(),
                            sentAt));
        }
    }
}
