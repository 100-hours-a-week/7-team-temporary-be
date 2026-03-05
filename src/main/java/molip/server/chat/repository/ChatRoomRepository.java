package molip.server.chat.repository;

import java.util.Optional;
import molip.server.chat.entity.ChatRoom;
import molip.server.common.enums.ChatRoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByTitleAndDeletedAtIsNull(String title);

    Page<ChatRoom> findByTitleContainingAndDeletedAtIsNullOrderByCreatedAtDesc(
            String title, Pageable pageable);

    Page<ChatRoom> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    Page<ChatRoom> findByTypeInAndDeletedAtIsNullOrderByCreatedAtDesc(
            java.util.List<ChatRoomType> types, Pageable pageable);

    Page<ChatRoom> findByTitleContainingAndTypeInAndDeletedAtIsNullOrderByCreatedAtDesc(
            String title, java.util.List<ChatRoomType> types, Pageable pageable);

    @Query(
            """
            select r
            from ChatRoom r
            join ChatRoomParticipant me
              on me.chatRoom = r
             and me.deletedAt is null
             and me.leftAt is null
             and me.user.id = :userId
            join ChatRoomParticipant oppositeUser
              on oppositeUser.chatRoom = r
             and oppositeUser.deletedAt is null
             and oppositeUser.leftAt is null
             and oppositeUser.user.id = :friendId
            where r.type = :type
              and r.deletedAt is null
            order by r.id desc
            """)
    Optional<ChatRoom> findDirectRoomByUserPair(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId,
            @Param("type") ChatRoomType type);
}
