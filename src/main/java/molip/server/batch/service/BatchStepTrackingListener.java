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

    public static final String CONTEXT_TARGET_TYPE = "batchTargetType";
    public static final String CONTEXT_TARGET_ID = "batchTargetId";

    private final BatchTrackingService trackingService;
    private final BatchTargetType targetType;
    private final Long targetId;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long jobRunId =
                stepExecution.getJobExecution().getExecutionContext().getLong("batchJobRunId");
        BatchTargetType resolvedTargetType = resolveTargetType(stepExecution);
        Long resolvedTargetId = resolveTargetId(stepExecution);
        BatchStepRun stepRun =
                trackingService.createStepRun(
                        trackingService.getJobRun(jobRunId),
                        stepExecution.getStepName(),
                        resolvedTargetType,
                        resolvedTargetId);
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

    private BatchTargetType resolveTargetType(StepExecution stepExecution) {
        Object value = stepExecution.getExecutionContext().get(CONTEXT_TARGET_TYPE);
        return value == null ? targetType : parseTargetType(value);
    }

    private Long resolveTargetId(StepExecution stepExecution) {
        Object value = stepExecution.getExecutionContext().get(CONTEXT_TARGET_ID);
        return value == null ? targetId : parseTargetId(value);
    }

    private BatchTargetType parseTargetType(Object value) {
        try {
            return BatchTargetType.valueOf(value.toString());
        } catch (IllegalArgumentException e) {
            return targetType;
        }
    }

    private Long parseTargetId(Object value) {
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return targetId;
        }
    }
}
