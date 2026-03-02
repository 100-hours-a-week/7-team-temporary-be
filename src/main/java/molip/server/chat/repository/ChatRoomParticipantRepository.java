package molip.server.chat.repository;

import java.util.List;
import java.util.Optional;
import molip.server.chat.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    @EntityGraph(attributePaths = {"user", "chatRoom"})
    List<ChatRoomParticipant>
            findAllByChatRoomIdAndDeletedAtIsNullAndLeftAtIsNullOrderByCreatedAtAsc(
                    Long chatRoomId);

    @EntityGraph(attributePaths = {"user", "chatRoom"})
    Optional<ChatRoomParticipant> findByChatRoomIdAndUserIdAndDeletedAtIsNullAndLeftAtIsNull(
            Long chatRoomId, Long userId);
}
