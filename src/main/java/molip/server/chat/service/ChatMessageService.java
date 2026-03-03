package molip.server.chat.service;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatMessage;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.repository.ChatMessageRepository;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Optional<ChatMessage> getLatestNonSystemMessage(Long chatRoomId) {
        return chatMessageRepository
                .findTopByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndMessageTypeNotOrderBySentAtDescIdDesc(
                        chatRoomId, MessageType.SYSTEM);
    }

    @Transactional(readOnly = true)
    public int countUnreadMessages(Long chatRoomId, Long lastSeenMessageId) {
        if (lastSeenMessageId == null) {
            return chatMessageRepository
                    .countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndMessageTypeNot(
                            chatRoomId, MessageType.SYSTEM);
        }

        return chatMessageRepository
                .countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdGreaterThanAndMessageTypeNot(
                        chatRoomId, lastSeenMessageId, MessageType.SYSTEM);
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

    @Transactional(readOnly = true)
    public void validateMessageInRoom(Long roomId, Long messageId) {
        if (roomId == null || messageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (!chatMessageRepository.existsByIdAndChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(
                messageId, roomId)) {
            throw new BaseException(ErrorCode.CONFLICT_MESSAGE_NOT_IN_ROOM);
        }
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessages(Long chatRoomId, Long cursor, int size) {
        validateGetMessages(chatRoomId, cursor, size);

        if (cursor == null) {
            return chatMessageRepository
                    .findByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
                            chatRoomId, PageRequest.of(0, size));
        } else {
            return chatMessageRepository
                    .findByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdLessThanOrderByIdDesc(
                            chatRoomId, cursor, PageRequest.of(0, size));
        }
    }

    private void validateGetMessages(Long chatRoomId, Long cursor, int size) {
        if (chatRoomId == null || size <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }

        if (cursor != null
                && !chatMessageRepository
                        .existsByIdAndChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(
                                cursor, chatRoomId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_CURSOR_RANGE);
        }
    }
}
