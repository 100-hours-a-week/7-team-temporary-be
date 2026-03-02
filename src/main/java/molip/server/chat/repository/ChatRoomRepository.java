package molip.server.chat.repository;

import molip.server.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByTitleAndDeletedAtIsNull(String title);

    Page<ChatRoom> findByTitleContainingAndDeletedAtIsNullOrderByCreatedAtDesc(
            String title, Pageable pageable);

    Page<ChatRoom> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}
