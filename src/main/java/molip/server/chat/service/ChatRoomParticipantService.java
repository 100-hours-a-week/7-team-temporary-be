package molip.server.chat.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import molip.server.chat.entity.ChatRoom;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.repository.ChatRoomParticipantRepository;
import molip.server.user.entity.Users;
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
}
