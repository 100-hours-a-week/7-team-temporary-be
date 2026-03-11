package molip.server.chat.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.event.ChatRoomCreatedEvent;
import molip.server.chat.event.ChatRoomDeletedEvent;
import molip.server.chat.event.ChatRoomUpdatedEvent;
import molip.server.chat.repository.ChatRoomRepository;
import molip.server.common.enums.ChatRoomType;
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

    private static final String DIRECT_ROOM_TITLE_PREFIX = "DIRECT:";
    private static final String DIRECT_ROOM_DESCRIPTION = "1:1 private chat room";

    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoom createChatRoom(
            Long ownerId, String title, String description, Integer maxParticipants) {
        return createChatRoom(ownerId, title, description, maxParticipants, null);
    }

    @Transactional
    public ChatRoom createChatRoom(
            Long ownerId,
            String title,
            String description,
            Integer maxParticipants,
            ChatRoomType type) {
        validateCreateChatRoom(ownerId, title, description, maxParticipants, type);

        validateDuplicatedTitle(title.trim());

        ChatRoomType resolvedType = resolveCreatableType(type);
        ChatRoom chatRoom =
                new ChatRoom(
                        ownerId, title.trim(), resolvedType, description.trim(), maxParticipants);

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
        return searchChatRooms(title, null, page, size);
    }

    @Transactional(readOnly = true)
    public Page<ChatRoom> searchChatRooms(String title, ChatRoomType type, int page, int size) {
        validateSearchChatRooms(page, size);
        List<ChatRoomType> visibleTypes = resolveVisibleTypes(type);

        if (title == null || title.isBlank()) {
            return chatRoomRepository.findByTypeInAndDeletedAtIsNullOrderByCreatedAtDesc(
                    visibleTypes, PageRequest.of(page - 1, size));
        }

        return chatRoomRepository
                .findByTitleContainingAndTypeInAndDeletedAtIsNullOrderByCreatedAtDesc(
                        title.trim(), visibleTypes, PageRequest.of(page - 1, size));
    }

    @Transactional(readOnly = true)
    public ChatRoom findDirectRoomByUserPair(Long userId, Long friendId) {
        validateDirectRoomUserPair(userId, friendId);

        return chatRoomRepository
                .findDirectRoomByUserPair(userId, friendId, ChatRoomType.DIRECT_CHAT)
                .orElse(null);
    }

    @Transactional
    public ChatRoom createDirectChatRoom(Long ownerId, Long friendId) {
        validateDirectRoomUserPair(ownerId, friendId);

        Long min = Math.min(ownerId, friendId);
        Long max = Math.max(ownerId, friendId);

        ChatRoom directRoom =
                new ChatRoom(
                        ownerId,
                        DIRECT_ROOM_TITLE_PREFIX + min + ":" + max,
                        ChatRoomType.DIRECT_CHAT,
                        DIRECT_ROOM_DESCRIPTION,
                        2);

        return chatRoomRepository.save(directRoom);
    }

    private void validateCreateChatRoom(
            Long ownerId,
            String title,
            String description,
            Integer maxParticipants,
            ChatRoomType type) {
        if (ownerId == null
                || title == null
                || title.isBlank()
                || description == null
                || description.isBlank()
                || maxParticipants == null
                || maxParticipants <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (type != null && type != ChatRoomType.OPEN_CHAT && type != ChatRoomType.CAM_STUDY) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_CHAT_TYPE);
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

    private ChatRoomType resolveCreatableType(ChatRoomType type) {
        if (type == null) {
            return ChatRoomType.OPEN_CHAT;
        }

        return type;
    }

    private java.util.List<ChatRoomType> resolveVisibleTypes(ChatRoomType type) {
        if (type == null) {
            return java.util.List.of(ChatRoomType.OPEN_CHAT, ChatRoomType.CAM_STUDY);
        }

        if (type != ChatRoomType.OPEN_CHAT && type != ChatRoomType.CAM_STUDY) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_CHAT_TYPE);
        }

        return java.util.List.of(type);
    }

    private void validateDirectRoomUserPair(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
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
