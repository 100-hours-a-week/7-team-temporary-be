package molip.server.chat.service;

import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.repository.ChatRoomRepository;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoom createChatRoom(
            Long ownerId, String title, String description, Integer maxParticipants) {
        validateCreateChatRoom(ownerId, title, description, maxParticipants);

        ChatRoom chatRoom =
                new ChatRoom(ownerId, title.trim(), description.trim(), maxParticipants);

        return chatRoomRepository.save(chatRoom);
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

    private void validateDeleteChatRoom(Long userId, Long roomId) {
        if (userId == null || roomId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private void validateDeletePermission(Long userId, ChatRoom chatRoom) {
        if (!userId.equals(chatRoom.getOwnerId())) {
            throw new BaseException(ErrorCode.FORBIDDEN_ROOM_DELETE);
        }
    }
}
