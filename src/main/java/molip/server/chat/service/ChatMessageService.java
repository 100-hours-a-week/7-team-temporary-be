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

    @Transactional
    public ChatMessage createUserMessage(
            ChatRoom chatRoom, MessageType messageType, String content, Long senderId) {
        validateCreateUserMessage(chatRoom, messageType, content, senderId);

        ChatMessage message =
                new ChatMessage(
                        chatRoom,
                        messageType,
                        normalizeContent(content),
                        false,
                        LocalDateTime.now(),
                        SenderType.USER,
                        senderId);

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

    @Transactional(readOnly = true)
    public ChatMessage getMessage(Long messageId) {
        if (messageId == null) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return chatMessageRepository
                .findById(messageId)
                .orElseThrow(() -> new BaseException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ChatMessage getActiveMessage(Long messageId) {
        if (messageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
        }

        ChatMessage message = getMessage(messageId);

        if (message.getDeletedAt() != null || message.isDeleted()) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return message;
    }

    @Transactional
    public ChatMessage deleteMessage(Long userId, Long roomId, Long messageId) {
        validateDeleteMessage(userId, roomId, messageId);

        ChatMessage message = getMessage(messageId);

        validateMessageBelongsToRoom(message, roomId);
        validateDeletableMessage(message, userId);

        message.deleteMessage();

        return message;
    }

    @Transactional
    public ChatMessage updateMessage(Long userId, Long roomId, Long messageId, String content) {
        validateUpdateMessage(userId, roomId, messageId, content);

        ChatMessage message = getActiveMessage(messageId);

        validateMessageBelongsToRoom(message, roomId);
        validateMessageOwnership(message, userId);
        validateMessageEditable(message);

        message.updateContent(normalizeUpdatedContent(content));

        return message;
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

    private void validateCreateUserMessage(
            ChatRoom chatRoom, MessageType messageType, String content, Long senderId) {
        if (chatRoom == null || messageType == null || senderId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }

        boolean hasContent = content != null && !content.isBlank();

        if (messageType == MessageType.SYSTEM || messageType == MessageType.FILE) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }

        if ((messageType == MessageType.TEXT || messageType == MessageType.TEXT_WITH_IMAGES)
                && !hasContent) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }

        if (messageType == MessageType.IMAGE && hasContent) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_SEND);
        }
    }

    private void validateUpdateMessage(Long userId, Long roomId, Long messageId, String content) {
        if (userId == null || roomId == null || messageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
        }

        if (content == null || content.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
        }
    }

    private void validateDeleteMessage(Long userId, Long roomId, Long messageId) {
        if (userId == null || roomId == null || messageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_DELETE);
        }
    }

    private void validateMessageBelongsToRoom(ChatMessage message, Long roomId) {
        if (!message.getChatRoom().getId().equals(roomId)) {
            throw new BaseException(ErrorCode.CONFLICT_MESSAGE_NOT_IN_ROOM);
        }
    }

    private void validateMessageOwnership(ChatMessage message, Long userId) {
        if (message.getSenderType() != SenderType.USER || !userId.equals(message.getSenderId())) {
            throw new BaseException(ErrorCode.FORBIDDEN_MESSAGE_UPDATE);
        }
    }

    private void validateMessageDeleteOwnership(ChatMessage message, Long userId) {
        if (message.getSenderType() != SenderType.USER || !userId.equals(message.getSenderId())) {
            throw new BaseException(ErrorCode.FORBIDDEN_MESSAGE_DELETE);
        }
    }

    private void validateMessageEditable(ChatMessage message) {
        if (message.getMessageType() != MessageType.TEXT
                && message.getMessageType() != MessageType.TEXT_WITH_IMAGES) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
        }
    }

    private void validateDeletableMessage(ChatMessage message, Long userId) {
        if (message.getDeletedAt() != null || message.isDeleted()) {
            throw new BaseException(ErrorCode.CONFLICT_MESSAGE_ALREADY_DELETED);
        }

        validateMessageDeleteOwnership(message, userId);

        if (message.getMessageType() == MessageType.SYSTEM) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_DELETE);
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }

        String normalized = content.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeUpdatedContent(String content) {
        String normalized = normalizeContent(content);

        if (normalized == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MESSAGE_UPDATE);
        }

        return normalized;
    }
}
