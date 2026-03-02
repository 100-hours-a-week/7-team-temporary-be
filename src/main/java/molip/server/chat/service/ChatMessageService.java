package molip.server.chat.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.repository.ChatMessageRepository;
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
}
