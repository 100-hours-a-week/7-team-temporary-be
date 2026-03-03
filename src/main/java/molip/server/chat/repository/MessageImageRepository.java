package molip.server.chat.repository;

import java.util.List;
import molip.server.chat.entity.MessageImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageImageRepository extends JpaRepository<MessageImage, Long> {

    @EntityGraph(attributePaths = {"image"})
    List<MessageImage> findAllByMessageIdInAndDeletedAtIsNullOrderBySortOrderAsc(
            List<Long> messageIds);
}
