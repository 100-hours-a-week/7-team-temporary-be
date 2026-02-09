package molip.server.batch.weekly;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
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
            Step weeklyAiNotifyStep) {
        return new JobBuilder("weeklyIngestJob", jobRepository)
                .start(weeklyScoreStep)
                .next(weeklyAiIngestStep)
                .next(weeklyAiNotifyStep)
                .build();
    }

    @Bean
    public Step weeklyScoreStep(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklyScoreStep", jobRepository)
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
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklyAiIngestStep", jobRepository)
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
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("weeklyAiNotifyStep", jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            log.info("Weekly AI notify step placeholder executed.");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }
}
