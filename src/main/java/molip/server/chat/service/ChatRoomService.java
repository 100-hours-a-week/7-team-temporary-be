package molip.server.chat.service;

import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.event.ChatRoomCreatedEvent;
import molip.server.chat.event.ChatRoomDeletedEvent;
import molip.server.chat.event.ChatRoomUpdatedEvent;
import molip.server.chat.repository.ChatRoomRepository;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoom createChatRoom(
            Long ownerId, String title, String description, Integer maxParticipants) {
        validateCreateChatRoom(ownerId, title, description, maxParticipants);

        validateDuplicatedTitle(title.trim());

        ChatRoom chatRoom =
                new ChatRoom(ownerId, title.trim(), description.trim(), maxParticipants);

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        eventPublisher.publishEvent(new ChatRoomCreatedEvent(savedChatRoom, ownerId));

        return savedChatRoom;
    }

    @Transactional
    public void deleteChatRoom(Long userId, Long roomId) {
        validateDeleteChatRoom(userId, roomId);

        ChatRoom chatRoom =
                chatRoomRepository
                        .findById(roomId)
                        .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_ROOM));

        if (chatRoom.getDeletedAt() != null) {
            throw new BaseException(ErrorCode.CONFLICT_ROOM_ALREADY_DELETED);
        }

        validateDeletePermission(userId, chatRoom);

        chatRoom.deleteRoom();

        eventPublisher.publishEvent(new ChatRoomDeletedEvent(chatRoom));
    }

    @Transactional
    public void updateChatRoom(
            Long userId, Long roomId, String title, String description, Integer maxParticipants) {
        validateUpdateChatRoom(userId, roomId, title, description, maxParticipants);

        ChatRoom chatRoom =
                chatRoomRepository
                        .findById(roomId)
                        .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_ROOM));

        if (chatRoom.getDeletedAt() != null) {
            throw new BaseException(ErrorCode.CONFLICT_ROOM_ALREADY_DELETED);
        }

        validateUpdatePermission(userId, chatRoom);

        chatRoom.updateRoom(title.trim(), description.trim(), maxParticipants);

        eventPublisher.publishEvent(new ChatRoomUpdatedEvent(chatRoom));
    }

    @Transactional(readOnly = true)
    public ChatRoom getChatRoom(Long roomId) {
        validateGetChatRoomDetail(roomId);

        ChatRoom chatRoom =
                chatRoomRepository
                        .findById(roomId)
                        .orElseThrow(() -> new BaseException(ErrorCode.ROOM_NOT_FOUND));

        if (chatRoom.getDeletedAt() != null) {
            throw new BaseException(ErrorCode.ROOM_NOT_FOUND);
        }

        return chatRoom;
    }

    @Transactional(readOnly = true)
    public Page<ChatRoom> searchChatRooms(String title, int page, int size) {
        validateSearchChatRooms(page, size);

        if (title == null || title.isBlank()) {
            return chatRoomRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(
                    PageRequest.of(page - 1, size));
        }

        return chatRoomRepository.findByTitleContainingAndDeletedAtIsNullOrderByCreatedAtDesc(
                title.trim(), PageRequest.of(page - 1, size));
    }

    private void validateCreateChatRoom(
            Long ownerId, String title, String description, Integer maxParticipants) {
        if (ownerId == null
                || title == null
                || title.isBlank()
                || description == null
                || description.isBlank()
                || maxParticipants == null
                || maxParticipants <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void validateDuplicatedTitle(String title) {
        if (chatRoomRepository.existsByTitleAndDeletedAtIsNull(title)) {
            throw new BaseException(ErrorCode.CONFLICT_CHAT_ROOM_TITLE);
        }
    }

    private void validateDeleteChatRoom(Long userId, Long roomId) {
        if (userId == null || roomId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void validateUpdateChatRoom(
            Long userId, Long roomId, String title, String description, Integer maxParticipants) {
        if (userId == null
                || roomId == null
                || title == null
                || title.isBlank()
                || description == null
                || description.isBlank()
                || maxParticipants == null
                || maxParticipants <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void validateGetChatRoomDetail(Long roomId) {
        if (roomId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void validateSearchChatRooms(int page, int size) {
        if (page <= 0 || size <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }

    private void validateDeletePermission(Long userId, ChatRoom chatRoom) {
        if (!userId.equals(chatRoom.getOwnerId())) {
            throw new BaseException(ErrorCode.FORBIDDEN_ROOM_DELETE);
        }
    }

    private void validateUpdatePermission(Long userId, ChatRoom chatRoom) {
        if (!userId.equals(chatRoom.getOwnerId())) {
            throw new BaseException(ErrorCode.FORBIDDEN_ROOM_UPDATE);
        }
    }
}
