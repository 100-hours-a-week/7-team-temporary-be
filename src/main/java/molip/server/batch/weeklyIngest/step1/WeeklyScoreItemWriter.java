package molip.server.batch.weeklyIngest.step1;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import molip.server.batch.entity.BatchJobRun;
import molip.server.batch.entity.BatchStepRun;
import molip.server.batch.enums.BatchStepStatus;
import molip.server.batch.enums.BatchTargetType;
import molip.server.batch.service.BatchTrackingService;
import molip.server.report.repository.ReportDailyStatRepository;
import molip.server.report.repository.ReportRepository;
import molip.server.schedule.repository.ScheduleRepository;
import molip.server.user.entity.Users;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

@RequiredArgsConstructor
public class WeeklyScoreItemWriter implements ItemWriter<Users>, StepExecutionListener {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final BatchTrackingService trackingService;
    private final ScheduleRepository scheduleRepository;
    private final ReportRepository reportRepository;
    private final ReportDailyStatRepository reportDailyStatRepository;

    private BatchJobRun jobRun;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long jobRunId =
                stepExecution.getJobExecution().getExecutionContext().getLong("batchJobRunId");
        this.jobRun = trackingService.getJobRun(jobRunId);

        LocalDate runDate = resolveRunDate(stepExecution);
        this.periodEnd = runDate.minusDays(1);
        this.periodStart = periodEnd.minusDays(6);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public void write(Chunk<? extends Users> items) {
        for (Users user : items) {
            BatchStepRun stepRun =
                    trackingService.createStepRun(
                            jobRun, "weeklyScoreStep", BatchTargetType.USER, user.getId());
            trackingService.markStepStarted(stepRun.getId());
            try {
                WeeklyScoreService.processUser(
                        user,
                        periodStart,
                        periodEnd,
                        scheduleRepository,
                        reportRepository,
                        reportDailyStatRepository);
                trackingService.markStepFinished(stepRun.getId(), BatchStepStatus.SUCCESS, null);
            } catch (Exception e) {
                trackingService.markStepFinished(
                        stepRun.getId(), BatchStepStatus.FAILED, e.getMessage());
                throw new IllegalStateException(
                        "Weekly score calculation failed for user " + user.getId(), e);
            }
        }
    }

    private LocalDate resolveRunDate(StepExecution stepExecution) {
        String runDateText =
                stepExecution.getJobExecution().getExecutionContext().getString("batchRunDate");

        if (runDateText == null || runDateText.isBlank()) {
            return LocalDate.now(ZONE_ID);
        }

        try {
            return LocalDate.parse(runDateText);
        } catch (DateTimeParseException e) {
            return LocalDate.now(ZONE_ID);
        }
    }
}
