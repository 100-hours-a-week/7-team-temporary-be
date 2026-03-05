package molip.server.batch.weeklyIngest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyIngestBatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeeklyIngestBatchScheduler.class);

    private final JobOperator jobOperator;
    private final Job weeklyIngestJob;

    // @Scheduled(cron = "0 0 22 ? * SUN", zone = "Asia/Seoul")
    @Scheduled(initialDelay = 300000, fixedDelay = 300000)
    public void runWeeklyIngestJob() {
        try {
            LocalDate runDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
            jobOperator.start(
                    weeklyIngestJob,
                    new JobParametersBuilder()
                            .addString("runAt", ZonedDateTime.now().toString(), false)
                            .addString("runDate", runDate.toString(), true)
                            .toJobParameters());
        } catch (Exception e) {
            log.error("Weekly ingest job failed to start.", e);
        }
    }
}
