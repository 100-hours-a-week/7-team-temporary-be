package molip.server.chat.repository;

import java.util.Optional;
import molip.server.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage>
            findTopByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseOrderBySentAtDescIdDesc(
                    Long chatRoomId);

    int countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(Long chatRoomId);

    int countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdGreaterThan(
            Long chatRoomId, Long lastSeenMessageId);
}
