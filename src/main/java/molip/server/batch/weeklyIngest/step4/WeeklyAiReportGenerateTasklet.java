package molip.server.batch.weeklyIngest.step4;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiWeeklyReportClient;
import molip.server.ai.dto.request.AiWeeklyReportGenerateRequest.UserReportTarget;
import molip.server.ai.dto.response.AiWeeklyReportGenerateResponse;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.entity.Report;
import molip.server.report.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyAiReportGenerateTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(WeeklyAiReportGenerateTasklet.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ReportRepository reportRepository;
    private final AiWeeklyReportClient aiWeeklyReportClient;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate runDate = resolveRunDate(chunkContext);
        LocalDate periodEnd = runDate.minusDays(1);
        LocalDate periodStart = periodEnd.minusDays(6);

        List<Report> reports = reportRepository.findWeeklyTargets(periodStart, periodEnd);
        List<UserReportTarget> targets = resolveTargets(reports);

        if (targets.isEmpty()) {
            log.info(
                    "Weekly AI report generate skipped. baseDate={}, reason=no targets",
                    periodStart);
            return RepeatStatus.FINISHED;
        }

        AiWeeklyReportGenerateResponse response =
                aiWeeklyReportClient.requestGenerate(periodStart, targets);

        if (!response.success()) {
            throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
        }

        log.info(
                "Weekly AI report generate succeeded. baseDate={}, targetCount={}, acknowledgedCount={}, message={}",
                periodStart,
                targets.size(),
                response.count(),
                response.message());

        return RepeatStatus.FINISHED;
    }

    private LocalDate resolveRunDate(ChunkContext chunkContext) {
        String runDateText =
                chunkContext
                        .getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext()
                        .getString("batchRunDate");

        if (runDateText == null || runDateText.isBlank()) {
            return LocalDate.now(ZONE_ID);
        }

        try {
            return LocalDate.parse(runDateText);
        } catch (DateTimeParseException e) {
            return LocalDate.now(ZONE_ID);
        }
    }

    private List<UserReportTarget> resolveTargets(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            return List.of();
        }

        Map<Long, UserReportTarget> deduplicated = new LinkedHashMap<>();

        for (Report report : reports) {
            if (report == null
                    || report.getId() == null
                    || report.getUser() == null
                    || report.getUser().getId() == null) {
                continue;
            }

            deduplicated.put(
                    report.getId(), UserReportTarget.of(report.getUser().getId(), report.getId()));
        }

        return deduplicated.values().stream().toList();
    }
}
