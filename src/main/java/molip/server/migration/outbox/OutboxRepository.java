package molip.server.migration.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.migration.event.DomainEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OutboxRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(DomainEvent event) {
        String payload = serializePayload(event);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OutboxEvent outboxEvent =
                new OutboxEvent(
                        event.eventId(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.eventType().name(),
                        event.eventVersion(),
                        event.occurredAt(),
                        payload,
                        OutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        now);
        outboxEventJpaRepository.save(outboxEvent);
    }

    public List<OutboxRecord> findPending(int limit) {
        return outboxEventJpaRepository
                .findByStatus(OutboxStatus.PENDING, PageRequest.of(0, limit))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    public List<OutboxRecord> findRetryableFailed(
            int limit, int maxRetryCount, OffsetDateTime retryBefore) {
        return outboxEventJpaRepository
                .findRetryableFailed(
                        OutboxStatus.FAILED, maxRetryCount, retryBefore, PageRequest.of(0, limit))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    public List<OutboxRecord> findDlqCandidates(int limit, int maxRetryCount) {
        return outboxEventJpaRepository
                .findDlqCandidates(OutboxStatus.FAILED, maxRetryCount, PageRequest.of(0, limit))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Transactional
    public void markSent(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        outboxEventJpaRepository.updateStatusByIds(
                OutboxStatus.SENT, OffsetDateTime.now(ZoneOffset.UTC), ids);
    }

    @Transactional
    public void markPending(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        outboxEventJpaRepository.updateStatusByIds(
                OutboxStatus.PENDING, OffsetDateTime.now(ZoneOffset.UTC), ids);
    }

    @Transactional
    public void markDlq(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        outboxEventJpaRepository.updateStatusByIds(
                OutboxStatus.DLQ, OffsetDateTime.now(ZoneOffset.UTC), ids);
    }

    @Transactional
    public void markFailed(Long id, String lastError) {
        outboxEventJpaRepository.markFailed(
                OutboxStatus.FAILED, lastError, OffsetDateTime.now(ZoneOffset.UTC), id);
    }

    private String serializePayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event.payload());
        } catch (JsonProcessingException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_REQUIRED_VALUES);
        }
    }

    private OutboxRecord toRecord(OutboxEvent event) {
        return new OutboxRecord(
                event.getId(),
                event.getEventId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                event.getEventVersion(),
                event.getOccurredAt(),
                event.getPayload(),
                event.getStatus(),
                event.getRetryCount(),
                event.getLastError(),
                event.getCreatedAt(),
                event.getUpdatedAt());
    }
}
