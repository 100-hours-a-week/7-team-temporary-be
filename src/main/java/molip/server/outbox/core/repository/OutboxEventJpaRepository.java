package molip.server.outbox.core.repository;

import java.time.OffsetDateTime;
import java.util.List;
import molip.server.outbox.core.entity.OutboxEvent;
import molip.server.outbox.core.model.OutboxStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("select e from OutboxEvent e where e.status = :status order by e.id asc")
    List<OutboxEvent> findByStatus(@Param("status") OutboxStatus status, PageRequest pageRequest);

    @Query(
            "select e from OutboxEvent e "
                    + "where e.aggregateType = :aggregateType "
                    + "and e.eventType = :eventType "
                    + "and e.aggregateId in :aggregateIds "
                    + "order by e.id desc")
    List<OutboxEvent> findLatestByAggregateIds(
            @Param("aggregateType") String aggregateType,
            @Param("eventType") String eventType,
            @Param("aggregateIds") List<String> aggregateIds);

    @Query(
            "select e from OutboxEvent e "
                    + "where e.status = :status and e.retryCount < :maxRetryCount and e.updatedAt <= :retryBefore "
                    + "order by e.id asc")
    List<OutboxEvent> findRetryableFailed(
            @Param("status") OutboxStatus status,
            @Param("maxRetryCount") int maxRetryCount,
            @Param("retryBefore") OffsetDateTime retryBefore,
            PageRequest pageRequest);

    @Query(
            "select e from OutboxEvent e "
                    + "where e.status = :status and e.retryCount >= :maxRetryCount "
                    + "order by e.id asc")
    List<OutboxEvent> findDlqCandidates(
            @Param("status") OutboxStatus status,
            @Param("maxRetryCount") int maxRetryCount,
            PageRequest pageRequest);

    @Modifying
    @Query(
            "update OutboxEvent e set e.status = :status, e.updatedAt = :updatedAt where e.id in :ids")
    int updateStatusByIds(
            @Param("status") OutboxStatus status,
            @Param("updatedAt") OffsetDateTime updatedAt,
            @Param("ids") List<Long> ids);

    @Modifying
    @Query(
            "update OutboxEvent e "
                    + "set e.status = :status, e.retryCount = e.retryCount + 1, e.lastError = :lastError, e.updatedAt = :updatedAt "
                    + "where e.id = :id")
    int markFailed(
            @Param("status") OutboxStatus status,
            @Param("lastError") String lastError,
            @Param("updatedAt") OffsetDateTime updatedAt,
            @Param("id") Long id);
}
