package molip.server.chat.repository;

import java.util.List;
import java.util.Optional;
import molip.server.chat.entity.ChatRoomParticipant;
import molip.server.chat.repository.projection.ChatRoomParticipantCountProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(
            """
            select
                p.chatRoom.id as chatRoomId,
                count(p) as participantsCount
            from ChatRoomParticipant p
            where p.chatRoom.id in :chatRoomIds
              and p.deletedAt is null
              and p.leftAt is null
            group by p.chatRoom.id
            """)
    List<ChatRoomParticipantCountProjection> countActiveParticipantsByChatRoomIds(
            @Param("chatRoomIds") List<Long> chatRoomIds);
}
