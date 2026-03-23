package molip.server.chat.event.handler;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.chat.dto.response.ChatMyRoomItemResponse;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatMessageRealtimePayloadFactory;
import molip.server.chat.event.ChatMessageSentCommittedEvent;
import molip.server.chat.event.ChatMessageSentEvent;
import molip.server.chat.facade.ChatRoomQueryFacade;
import molip.server.chat.redis.presence.RedisChatParticipantPresenceStore;
import molip.server.chat.service.ChatRoomParticipantService;
import molip.server.common.enums.MessageType;
import molip.server.notification.event.ChatMessageNotificationRequestedEvent;
import molip.server.notification.metrics.ChatMessageAlertMetrics;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;
import molip.server.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSentEventHandler {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatRoomQueryFacade chatRoomQueryFacade;
    private final ChatMessageRealtimePayloadFactory chatMessageRealtimePayloadFactory;
    private final RedisChatParticipantPresenceStore redisChatParticipantPresenceStore;
    private final UserService userService;
    private final ChatMessageAlertMetrics chatMessageAlertMetrics;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(ChatMessageSentEvent event) {
        log.info(
                "handle chat message sent event: roomId={}, messageId={}, senderUserId={}",
                event.chatRoom().getId(),
                event.message().getId(),
                event.senderUserId());

        List<ChatRoomParticipant> activeParticipants =
                chatRoomParticipantService.getActiveParticipants(event.chatRoom().getId());
        int participantsCount = activeParticipants.size();

        ChatMessageCreatedResponse messageCreated =
                chatMessageRealtimePayloadFactory.buildMessageCreated(
                        event.message(), event.images());

        List<SocketUnreadChangedResponse> unreadChanges =
                activeParticipants.stream()
                        .map(
                                participant ->
                                        buildUnreadChanged(
                                                event.chatRoom().getId(),
                                                participant,
                                                event.message(),
                                                participantsCount))
                        .toList();

        publishChatMessageNotifications(
                event.chatRoom().getId(),
                activeParticipants,
                event.message(),
                event.senderUserId());

        eventPublisher.publishEvent(
                new ChatMessageSentCommittedEvent(
                        event.chatRoom().getId(), messageCreated, unreadChanges));
    }

    private SocketUnreadChangedResponse buildUnreadChanged(
            Long roomId,
            ChatRoomParticipant participant,
            ChatMessage latestMessage,
            int participantsCount) {
        ChatMyRoomItemResponse row =
                chatRoomQueryFacade.buildMyChatRoomItem(
                        participant, latestMessage, participantsCount);
        Long userId = participant.getUser().getId();
        log.info(
                "publish unreadChanged for message send: roomId={}, targetUserId={}, unreadCount={}",
                roomId,
                userId,
                row.unreadCount());

        return SocketUnreadChangedResponse.of(
                UUID.randomUUID().toString(),
                userId,
                roomId,
                row.unreadCount(),
                row.lastUserMessagePreview(),
                row.lastUserMessageSentAt(),
                row.participantsCount());
    }

    private void publishChatMessageNotifications(
            Long roomId,
            List<ChatRoomParticipant> activeParticipants,
            ChatMessage message,
            Long senderUserId) {
        if (senderUserId == null) {
            return;
        }

        String senderNickname = userService.getUser(senderUserId).getNickname();
        String preview = resolveMessagePreview(message);
        int offlineTargets = 0;
        int requestedCount = 0;

        for (ChatRoomParticipant participant : activeParticipants) {
            Long targetUserId = participant.getUser().getId();
            if (targetUserId.equals(senderUserId)) {
                continue;
            }

            if (redisChatParticipantPresenceStore.isOnline(roomId, participant.getId())) {
                continue;
            }

            offlineTargets++;
            requestedCount++;
            eventPublisher.publishEvent(
                    new ChatMessageNotificationRequestedEvent(
                            targetUserId, roomId, senderNickname, preview));
        }

        chatMessageAlertMetrics.recordMessageFanout(offlineTargets, requestedCount);
        log.info(
                "chat message alert fanout: roomId={}, messageId={}, offlineTargets={}, requested={}",
                roomId,
                message.getId(),
                offlineTargets,
                requestedCount);
    }

    private String resolveMessagePreview(ChatMessage message) {
        if (message.getMessageType() == MessageType.TEXT
                && message.getContent() != null
                && !message.getContent().isBlank()) {
            return message.getContent();
        }

        return "새 메시지가 도착했습니다.";
    }
}
