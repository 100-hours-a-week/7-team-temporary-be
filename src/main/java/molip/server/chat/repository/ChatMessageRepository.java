package molip.server.chat.repository;

import java.util.Optional;
import molip.server.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    boolean existsByIdAndChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(Long id, Long chatRoomId);

    Page<ChatMessage> findByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
            Long chatRoomId, Pageable pageable);

    Page<ChatMessage> findByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdLessThanOrderByIdDesc(
            Long chatRoomId, Long cursor, Pageable pageable);
}
