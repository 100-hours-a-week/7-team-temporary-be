package molip.server.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.batch.enums.BatchStepStatus;
import molip.server.batch.enums.BatchTargetType;
import molip.server.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BatchStepRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_run_id", nullable = false)
    private BatchJobRun jobRun;

    @Column(nullable = false, length = 100)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchTargetType targetType;

    @Column private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStepStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 500)
    private String lastError;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    public BatchStepRun(
            BatchJobRun jobRun,
            String stepName,
            BatchTargetType targetType,
            Long targetId,
            BatchStepStatus status,
            int retryCount,
            String lastError,
            LocalDateTime startedAt,
            LocalDateTime finishedAt) {
        this.jobRun = jobRun;
        this.stepName = stepName;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public void updateStatus(BatchStepStatus status) {
        this.status = status;
    }

    public void markStarted(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        this.status = BatchStepStatus.RUNNING;
    }

    public void markFinished(LocalDateTime finishedAt, BatchStepStatus status) {
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
