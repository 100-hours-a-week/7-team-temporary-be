package molip.server.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.repository.ChatRoomParticipantRepository;
import molip.server.chat.repository.projection.ChatRoomParticipantCountProjection;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    @Transactional
    public ChatRoomParticipant createOwnerParticipant(Users user, ChatRoom chatRoom) {
        ChatRoomParticipant participant = new ChatRoomParticipant(user, chatRoom, false);

        return chatRoomParticipantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomParticipant> getActiveParticipants(Long chatRoomId) {

        return chatRoomParticipantRepository
                .findAllByChatRoomIdAndDeletedAtIsNullAndLeftAtIsNullOrderByCreatedAtAsc(
                        chatRoomId);
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoomParticipant> getActiveParticipant(Long chatRoomId, Long userId) {
        return chatRoomParticipantRepository
                .findByChatRoomIdAndUserIdAndDeletedAtIsNullAndLeftAtIsNull(chatRoomId, userId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> countActiveParticipantsByChatRoomIds(List<Long> chatRoomIds) {
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return chatRoomParticipantRepository
                .countActiveParticipantsByChatRoomIds(chatRoomIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                ChatRoomParticipantCountProjection::getChatRoomId,
                                count -> Math.toIntExact(count.getParticipantsCount())));
    }

    @Transactional(readOnly = true)
    public Page<ChatRoomParticipant> getMyActiveParticipations(
            Long userId, ChatRoomType type, int page, int size) {
        validateGetMyActiveParticipations(userId, type, page, size);

        return chatRoomParticipantRepository.findActiveParticipationsByUserIdAndChatRoomType(
                userId, type, PageRequest.of(page - 1, size));
    }

    private void validateGetMyActiveParticipations(
            Long userId, ChatRoomType type, int page, int size) {
        if (userId == null || type == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_CHAT_TYPE);
        }

        if (page <= 0 || size <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }
}
