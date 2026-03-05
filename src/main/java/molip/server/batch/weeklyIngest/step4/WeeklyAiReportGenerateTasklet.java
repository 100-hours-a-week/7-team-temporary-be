package molip.server.batch.weeklyIngest.step4;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
    private static final String CONTEXT_STEP4_BASE_DATE = "step4BaseDate";
    private static final String CONTEXT_STEP4_REQUESTED_AT = "step4RequestedAt";
    private static final String CONTEXT_STEP4_TARGET_COUNT = "step4TargetCount";
    private static final String CONTEXT_STEP4_TARGET_PAIRS = "step4TargetPairs";

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
            saveStep4Context(chunkContext, periodStart, List.of());
            return RepeatStatus.FINISHED;
        }

        log.info(
                "Weekly AI report generate request prepared. baseDate={}, targetCount={}, sampleTarget={}",
                periodStart,
                targets.size(),
                targets.getFirst());

        AiWeeklyReportGenerateResponse response =
                aiWeeklyReportClient.requestGenerate(periodStart, targets);

        if (!response.success()) {
            throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
        }

        if (response.count() != targets.size()) {
            log.warn(
                    "Weekly AI report generate acknowledged count mismatch. baseDate={}, requestedCount={}, acknowledgedCount={}",
                    periodStart,
                    targets.size(),
                    response.count());
        }

        saveStep4Context(chunkContext, periodStart, targets);

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

    private void saveStep4Context(
            ChunkContext chunkContext, LocalDate baseDate, List<UserReportTarget> targets) {
        List<String> targetPairs = new ArrayList<>();

        for (UserReportTarget target : targets) {
            targetPairs.add(target.userId() + ":" + target.reportId());
        }

        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putString(CONTEXT_STEP4_BASE_DATE, baseDate.toString());

        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putString(CONTEXT_STEP4_REQUESTED_AT, LocalDateTime.now(ZONE_ID).toString());

        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putInt(CONTEXT_STEP4_TARGET_COUNT, targets.size());

        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put(CONTEXT_STEP4_TARGET_PAIRS, targetPairs);
    }
}
