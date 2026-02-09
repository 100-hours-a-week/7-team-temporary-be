package molip.server.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.batch.enums.BatchRunStatus;
import molip.server.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BatchJobRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;

    private LocalDate runDate;

    @Enumerated(EnumType.STRING)
    private BatchRunStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private int retryCount;

    private String lastError;

    public BatchJobRun(
            String jobName,
            LocalDate runDate,
            BatchRunStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            int retryCount,
            String lastError) {
        this.jobName = jobName;
        this.runDate = runDate;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.retryCount = retryCount;
        this.lastError = lastError;
    }

    public void updateStatus(BatchRunStatus status) {
        this.status = status;
    }

    public void markStarted(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        this.status = BatchRunStatus.RUNNING;
    }

    public void markFinished(LocalDateTime finishedAt, BatchRunStatus status) {
        this.finishedAt = finishedAt;
        this.status = status;
    }

    public void increaseRetryCount() {
        this.retryCount += 1;
    }

    public void updateLastError(String lastError) {
        this.lastError = lastError;
    }
}
