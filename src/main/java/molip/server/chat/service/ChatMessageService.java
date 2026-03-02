package molip.server.chat.service;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.repository.ChatMessageRepository;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public Optional<ChatMessage> getLatestMessage(Long chatRoomId) {
        return chatMessageRepository
                .findTopByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseOrderBySentAtDescIdDesc(
                        chatRoomId);
    }

    @Transactional(readOnly = true)
    public int countUnreadMessages(Long chatRoomId, Long lastSeenMessageId) {
        if (lastSeenMessageId == null) {
            return chatMessageRepository.countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(
                    chatRoomId);
        }

        return chatMessageRepository
                .countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdGreaterThan(
                        chatRoomId, lastSeenMessageId);
    }

    @Transactional
    public ChatMessage createSystemMessage(ChatRoom chatRoom, String content) {
        ChatMessage message =
                new ChatMessage(
                        chatRoom,
                        MessageType.SYSTEM,
                        content,
                        false,
                        LocalDateTime.now(),
                        SenderType.SYSTEM,
                        null);

        return chatMessageRepository.save(message);
    }
}
