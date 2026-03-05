package molip.server.batch.weeklyIngest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import molip.server.batch.entity.BatchJobRun;
import molip.server.batch.enums.BatchRunStatus;
import molip.server.batch.enums.BatchTargetType;
import molip.server.batch.service.BatchStepTrackingListener;
import molip.server.batch.service.BatchTrackingService;
import molip.server.batch.weeklyIngest.step1.WeeklyScoreItemWriter;
import molip.server.batch.weeklyIngest.step1.WeeklyScoreUserReader;
import molip.server.batch.weeklyIngest.step2.WeeklyAiIngestItemWriter;
import molip.server.batch.weeklyIngest.step3.WeeklyAiNotifyTasklet;
import molip.server.batch.weeklyIngest.step4.WeeklyAiReportGenerateTasklet;
import molip.server.report.repository.ReportDailyStatRepository;
import molip.server.report.repository.ReportRepository;
import molip.server.schedule.repository.DayPlanRepository;
import molip.server.schedule.repository.ScheduleHistoryRepository;
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
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
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
            Step weeklyAiReportGenerateStep,
            BatchTrackingService batchTrackingService) {
        return new JobBuilder("weeklyIngestJob", jobRepository)
                .listener(weeklyIngestJobListener(batchTrackingService))
                .start(weeklyScoreStep)
                .next(weeklyAiIngestStep)
                .next(weeklyAiNotifyStep)
                .next(weeklyAiReportGenerateStep)
                .build();
    }

    @Bean
    public JobExecutionListener weeklyIngestJobListener(BatchTrackingService batchTrackingService) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                LocalDate runDate = resolveRunDate(jobExecution);
                BatchJobRun jobRun =
                        batchTrackingService.createJobRun(
                                jobExecution.getJobInstance().getJobName(), runDate);
                batchTrackingService.markJobStarted(jobRun.getId());
                jobExecution.getExecutionContext().putLong("batchJobRunId", jobRun.getId());
                jobExecution.getExecutionContext().putString("batchRunDate", runDate.toString());
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
                                : jobExecution.getAllFailureExceptions().getFirst().getMessage();
                batchTrackingService.markJobFinished(jobRunId, status, lastError);
            }

            private LocalDate resolveRunDate(JobExecution jobExecution) {
                String runDateParam = jobExecution.getJobParameters().getString("runDate");
                if (runDateParam == null || runDateParam.isBlank()) {
                    return LocalDate.now(ZoneId.of("Asia/Seoul"));
                }

                try {
                    return LocalDate.parse(runDateParam);
                } catch (DateTimeParseException e) {
                    log.warn("Invalid runDate parameter: {}. Fallback to now.", runDateParam);
                    return LocalDate.now(ZoneId.of("Asia/Seoul"));
                }
            }
        };
    }

    @Bean
    public Step weeklyScoreStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            WeeklyScoreUserReader weeklyScoreUserReader,
            ItemWriter<Users> weeklyScoreItemWriter) {
        return new StepBuilder("weeklyScoreStep", jobRepository)
                .<Users, Users>chunk(1000)
                .transactionManager(transactionManager)
                .reader(weeklyScoreUserReader)
                .writer(weeklyScoreItemWriter)
                .build();
    }

    @Bean
    public Step weeklyAiIngestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            WeeklyScoreUserReader weeklyAiIngestUserReader,
            ItemWriter<Users> weeklyAiIngestItemWriter) {
        return new StepBuilder("weeklyAiIngestStep", jobRepository)
                .<Users, Users>chunk(1000)
                .transactionManager(transactionManager)
                .reader(weeklyAiIngestUserReader)
                .writer(weeklyAiIngestItemWriter)
                .build();
    }

    @Bean
    public Step weeklyAiNotifyStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatchTrackingService batchTrackingService,
            WeeklyAiNotifyTasklet weeklyAiNotifyTasklet) {
        return new StepBuilder("weeklyAiNotifyStep", jobRepository)
                .listener(
                        new molip.server.batch.service.BatchStepTrackingListener(
                                batchTrackingService, BatchTargetType.CHUNK, null))
                .tasklet(weeklyAiNotifyTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step weeklyAiReportGenerateStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatchTrackingService batchTrackingService,
            WeeklyAiReportGenerateTasklet weeklyAiReportGenerateTasklet) {
        return new StepBuilder("weeklyAiReportGenerateStep", jobRepository)
                .listener(
                        new BatchStepTrackingListener(
                                batchTrackingService, BatchTargetType.CHUNK, null))
                .tasklet(weeklyAiReportGenerateTasklet, transactionManager)
                .build();
    }

    @Bean
    public WeeklyScoreUserReader weeklyScoreUserReader(UserRepository userRepository) {
        return new WeeklyScoreUserReader(userRepository);
    }

    @Bean
    public WeeklyScoreUserReader weeklyAiIngestUserReader(UserRepository userRepository) {
        return new WeeklyScoreUserReader(userRepository);
    }

    @Bean
    public WeeklyScoreItemWriter weeklyScoreItemWriter(
            BatchTrackingService batchTrackingService,
            ScheduleRepository scheduleRepository,
            ReportRepository reportRepository,
            ReportDailyStatRepository reportDailyStatRepository) {
        return new WeeklyScoreItemWriter(
                batchTrackingService,
                scheduleRepository,
                reportRepository,
                reportDailyStatRepository);
    }

    @Bean
    public WeeklyAiIngestItemWriter weeklyAiIngestItemWriter(
            BatchTrackingService batchTrackingService,
            DayPlanRepository dayPlanRepository,
            ScheduleRepository scheduleRepository,
            ScheduleHistoryRepository scheduleHistoryRepository,
            @Qualifier("aiJdbcTemplate") JdbcTemplate aiJdbcTemplate) {
        return new WeeklyAiIngestItemWriter(
                batchTrackingService,
                dayPlanRepository,
                scheduleRepository,
                scheduleHistoryRepository,
                aiJdbcTemplate);
    }
}
