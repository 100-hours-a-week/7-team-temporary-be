package molip.server.batch.weekly;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.batch.entity.BatchJobRun;
import molip.server.batch.enums.BatchRunStatus;
import molip.server.batch.enums.BatchTargetType;
import molip.server.batch.service.BatchStepTrackingListener;
import molip.server.batch.service.BatchTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class WeeklyIngestBatchConfig {

    private static final Logger log = LoggerFactory.getLogger(WeeklyIngestBatchConfig.class);

    @Bean
    public Job weeklyIngestJob(
            JobRepository jobRepository,
            Step weeklyScoreStep,
            Step weeklyAiIngestStep,
            Step weeklyAiNotifyStep,
            BatchTrackingService batchTrackingService) {
        return new JobBuilder("weeklyIngestJob", jobRepository)
                .listener(weeklyIngestJobListener(batchTrackingService))
                .start(weeklyScoreStep)
                .next(weeklyAiIngestStep)
                .next(weeklyAiNotifyStep)
                .build();
    }

    @Bean
    public JobExecutionListener weeklyIngestJobListener(BatchTrackingService batchTrackingService) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                LocalDate runDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
                BatchJobRun jobRun =
                        batchTrackingService.createJobRun(
                                jobExecution.getJobInstance().getJobName(), runDate);
                batchTrackingService.markJobStarted(jobRun.getId());
                jobExecution.getExecutionContext().putLong("batchJobRunId", jobRun.getId());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                Long jobRunId = jobExecution.getExecutionContext().getLong("batchJobRunId");
                BatchRunStatus status =
                        jobExecution.getAllFailureExceptions().isEmpty()
                                ? BatchRunStatus.SUCCESS
                                : BatchRunStatus.FAILED;
                String lastError =
                        jobExecution.getAllFailureExceptions().isEmpty()
                                ? null
                                : jobExecution.getAllFailureExceptions().get(0).getMessage();
                batchTrackingService.markJobFinished(jobRunId, status, lastError);
            }
        };
    }

    @Bean
    public Step weeklyScoreStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatchTrackingService batchTrackingService) {
        return new StepBuilder("weeklyScoreStep", jobRepository)
                .listener(
                        new BatchStepTrackingListener(
                                batchTrackingService, BatchTargetType.GLOBAL, null))
                .tasklet(
                        (contribution, chunkContext) -> {
                            log.info("Weekly score step placeholder executed.");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }

    @Bean
    public Step weeklyAiIngestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatchTrackingService batchTrackingService) {
        return new StepBuilder("weeklyAiIngestStep", jobRepository)
                .listener(
                        new BatchStepTrackingListener(
                                batchTrackingService, BatchTargetType.GLOBAL, null))
                .tasklet(
                        (contribution, chunkContext) -> {
                            log.info("Weekly AI ingest step placeholder executed.");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }

    @Bean
    public Step weeklyAiNotifyStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatchTrackingService batchTrackingService) {
        return new StepBuilder("weeklyAiNotifyStep", jobRepository)
                .listener(
                        new BatchStepTrackingListener(
                                batchTrackingService, BatchTargetType.GLOBAL, null))
                .tasklet(
                        (contribution, chunkContext) -> {
                            log.info("Weekly AI notify step placeholder executed.");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }
}
