package molip.server.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.event.ChatRoomParticipantLeftEvent;
import molip.server.chat.repository.ChatRoomParticipantRepository;
import molip.server.chat.repository.projection.ChatRoomParticipantCountProjection;
import molip.server.common.enums.ChatRoomType;
import molip.server.common.enums.MessageType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatRoomParticipant createOwnerParticipant(Users user, ChatRoom chatRoom) {
        ChatRoomParticipant participant = new ChatRoomParticipant(user, chatRoom, null, false);

        return chatRoomParticipantRepository.save(participant);
    }

    @Transactional
    public ChatRoomParticipant createParticipant(
            Users user, ChatRoom chatRoom, Long lastSeenMessageId) {
        validateCreateParticipant(user, chatRoom);

        ChatRoomParticipant participant =
                new ChatRoomParticipant(user, chatRoom, lastSeenMessageId, false);

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
    public Optional<ChatRoomParticipant> getLatestParticipant(Long chatRoomId, Long userId) {
        return chatRoomParticipantRepository
                .findTopByChatRoomIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        chatRoomId, userId);
    }

    @Transactional(readOnly = true)
    public ChatRoomParticipant getActiveParticipantByIdAndRoomId(Long participantId, Long roomId) {
        if (participantId == null || roomId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        return chatRoomParticipantRepository
                .findByIdAndChatRoomIdAndDeletedAtIsNullAndLeftAtIsNull(participantId, roomId)
                .orElseThrow(() -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoomParticipant> findById(Long participantId) {
        return chatRoomParticipantRepository.findById(participantId);
    }

    @Transactional(readOnly = true)
    public ChatRoomParticipant getActiveParticipantById(Long participantId) {
        if (participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        return chatRoomParticipantRepository
                .findByIdAndDeletedAtIsNullAndLeftAtIsNull(participantId)
                .orElseThrow(() -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));
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
                userId, type, MessageType.SYSTEM, PageRequest.of(page - 1, size));
    }

    @Transactional(readOnly = true)
    public Set<Long> findJoinedChatRoomIds(Long userId, List<Long> chatRoomIds) {
        if (userId == null || chatRoomIds == null || chatRoomIds.isEmpty()) {
            return Collections.emptySet();
        }

        return chatRoomParticipantRepository
                .findActiveChatRoomIdsByUserIdAndChatRoomIds(userId, chatRoomIds)
                .stream()
                .collect(Collectors.toSet());
    }

    @Transactional
    public void updateLastSeenMessageId(ChatRoomParticipant participant, Long lastSeenMessageId) {
        validateUpdateLastSeenMessageId(participant, lastSeenMessageId);

        participant.updateLastSeenMessageId(lastSeenMessageId);
    }

    @Transactional
    public void leaveParticipant(ChatRoomParticipant participant) {
        validateLeaveParticipant(participant);

        participant.leave();
    }

    @Transactional
    public void leaveChatRoom(Long userId, Long roomId, Long participantId) {
        ChatRoomParticipant participant = validateLeaveChatRoom(userId, roomId, participantId);

        participant.leave();

        eventPublisher.publishEvent(
                new ChatRoomParticipantLeftEvent(
                        participant.getChatRoom(), participant, participant.getUser()));
    }

    private void validateCreateParticipant(Users user, ChatRoom chatRoom) {
        if (user == null || chatRoom == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (getActiveParticipant(chatRoom.getId(), user.getId()).isPresent()) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_PARTICIPATED);
        }

        int participantsCount =
                countActiveParticipantsByChatRoomIds(List.of(chatRoom.getId()))
                        .getOrDefault(chatRoom.getId(), 0);

        if (participantsCount >= chatRoom.getMaxParticipants()) {
            throw new BaseException(ErrorCode.CONFLICT_ROOM_FULL);
        }
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

    private void validateUpdateLastSeenMessageId(
            ChatRoomParticipant participant, Long lastSeenMessageId) {
        if (participant == null || lastSeenMessageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (participant.getLastSeenMessageId() != null
                && lastSeenMessageId < participant.getLastSeenMessageId()) {
            throw new BaseException(ErrorCode.CONFLICT_LAST_SEEN_DECREASE);
        }
    }

    private void validateLeaveParticipant(ChatRoomParticipant participant) {
        if (participant == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        if (participant.getLeftAt() != null) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_LEFT);
        }
    }

    private ChatRoomParticipant validateLeaveChatRoom(
            Long userId, Long roomId, Long participantId) {
        if (userId == null || roomId == null || participantId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }

        ChatRoomParticipant participant =
                chatRoomParticipantRepository
                        .findById(participantId)
                        .orElseThrow(() -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));

        if (!participant.getChatRoom().getId().equals(roomId)) {
            throw new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        if (!participant.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN_PARTICIPANT_REMOVE);
        }

        if (participant.getChatRoom().getType() == ChatRoomType.DIRECT_CHAT) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_DIRECT_CHAT_LEAVE);
        }

        if (participant.getLeftAt() != null) {
            throw new BaseException(ErrorCode.CONFLICT_ALREADY_LEFT);
        }

        return participant;
    }
}
