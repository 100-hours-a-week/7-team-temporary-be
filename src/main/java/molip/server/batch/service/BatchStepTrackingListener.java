package molip.server.batch.service;

import lombok.RequiredArgsConstructor;
import molip.server.batch.entity.BatchStepRun;
import molip.server.batch.enums.BatchStepStatus;
import molip.server.batch.enums.BatchTargetType;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

@RequiredArgsConstructor
public class BatchStepTrackingListener implements StepExecutionListener {

    private final BatchTrackingService trackingService;
    private final BatchTargetType targetType;
    private final Long targetId;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long jobRunId =
                stepExecution.getJobExecution().getExecutionContext().getLong("batchJobRunId");
        BatchStepRun stepRun =
                trackingService.createStepRun(
                        trackingService.getJobRun(jobRunId),
                        stepExecution.getStepName(),
                        targetType,
                        targetId);
        trackingService.markStepStarted(stepRun.getId());
        stepExecution.getExecutionContext().putLong("batchStepRunId", stepRun.getId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Long stepRunId = stepExecution.getExecutionContext().getLong("batchStepRunId");
        BatchStepStatus status =
                stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)
                        ? BatchStepStatus.SUCCESS
                        : BatchStepStatus.FAILED;
        String lastError =
                stepExecution.getFailureExceptions().isEmpty()
                        ? null
                        : stepExecution.getFailureExceptions().get(0).getMessage();
        trackingService.markStepFinished(stepRunId, status, lastError);
        return stepExecution.getExitStatus();
    }
}
