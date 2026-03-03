package molip.server.chat.repository;

import java.util.Optional;
import molip.server.chat.entity.ChatMessage;
import molip.server.common.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage>
            findTopByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseOrderBySentAtDescIdDesc(
                    Long chatRoomId);

    Optional<ChatMessage>
            findTopByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndMessageTypeNotOrderBySentAtDescIdDesc(
                    Long chatRoomId, MessageType messageType);

    int countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(Long chatRoomId);

    int countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndMessageTypeNot(
            Long chatRoomId, MessageType messageType);

    int countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdGreaterThan(
            Long chatRoomId, Long lastSeenMessageId);

    int countByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdGreaterThanAndMessageTypeNot(
            Long chatRoomId, Long lastSeenMessageId, MessageType messageType);

    boolean existsByIdAndChatRoomIdAndDeletedAtIsNullAndIsDeletedFalse(Long id, Long chatRoomId);

    Page<ChatMessage> findByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
            Long chatRoomId, Pageable pageable);

    Page<ChatMessage> findByChatRoomIdAndDeletedAtIsNullAndIsDeletedFalseAndIdLessThanOrderByIdDesc(
            Long chatRoomId, Long cursor, Pageable pageable);
}
