package molip.server.batch.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import molip.server.batch.entity.BatchJobRun;
import molip.server.batch.entity.BatchStepRun;
import molip.server.batch.enums.BatchRunStatus;
import molip.server.batch.enums.BatchStepStatus;
import molip.server.batch.enums.BatchTargetType;
import molip.server.batch.repository.BatchJobRunRepository;
import molip.server.batch.repository.BatchStepRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BatchTrackingService {

    private final BatchJobRunRepository jobRunRepository;
    private final BatchStepRunRepository stepRunRepository;

    @Transactional
    public BatchJobRun createJobRun(String jobName, LocalDate runDate) {
        BatchJobRun jobRun =
                new BatchJobRun(jobName, runDate, BatchRunStatus.PENDING, null, null, 0, null);
        return jobRunRepository.save(jobRun);
    }

    @Transactional
    public BatchJobRun markJobStarted(Long jobRunId) {
        BatchJobRun jobRun = getJobRun(jobRunId);

        jobRun.markStarted(LocalDateTime.now());

        return jobRun;
    }

    @Transactional
    public BatchJobRun markJobFinished(Long jobRunId, BatchRunStatus status, String lastError) {
        BatchJobRun jobRun = getJobRun(jobRunId);

        jobRun.markFinished(LocalDateTime.now(), status);

        if (lastError != null && !lastError.isBlank()) {
            jobRun.updateLastError(lastError);
        }

        return jobRun;
    }

    @Transactional
    public BatchStepRun createStepRun(
            BatchJobRun jobRun, String stepName, BatchTargetType targetType, Long targetId) {
        BatchStepRun stepRun =
                new BatchStepRun(
                        jobRun,
                        stepName,
                        targetType,
                        targetId,
                        BatchStepStatus.PENDING,
                        0,
                        null,
                        null,
                        null);
        return stepRunRepository.save(stepRun);
    }

    @Transactional
    public BatchStepRun markStepStarted(Long stepRunId) {
        BatchStepRun stepRun = getStepRun(stepRunId);
        stepRun.markStarted(LocalDateTime.now());
        return stepRun;
    }

    @Transactional
    public BatchStepRun markStepFinished(Long stepRunId, BatchStepStatus status, String lastError) {
        BatchStepRun stepRun = getStepRun(stepRunId);
        stepRun.markFinished(LocalDateTime.now(), status);
        if (lastError != null && !lastError.isBlank()) {
            stepRun.updateLastError(lastError);
        }
        return stepRun;
    }

    @Transactional
    public BatchStepRun increaseStepRetry(Long stepRunId, String lastError) {
        BatchStepRun stepRun = getStepRun(stepRunId);
        stepRun.increaseRetryCount();
        stepRun.updateStatus(BatchStepStatus.RETRYING);
        if (lastError != null && !lastError.isBlank()) {
            stepRun.updateLastError(lastError);
        }
        return stepRun;
    }

    public BatchJobRun getJobRun(Long jobRunId) {
        return jobRunRepository
                .findById(jobRunId)
                .orElseThrow(() -> new IllegalArgumentException("Batch job run not found."));
    }

    private BatchStepRun getStepRun(Long stepRunId) {
        return stepRunRepository
                .findById(stepRunId)
                .orElseThrow(() -> new IllegalArgumentException("Batch step run not found."));
    }
}
