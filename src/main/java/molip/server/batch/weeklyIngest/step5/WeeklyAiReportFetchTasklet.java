package molip.server.batch.weeklyIngest.step5;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiWeeklyReportFetchClient;
import molip.server.ai.dto.request.AiWeeklyReportFetchRequest.WeeklyReportFetchTarget;
import molip.server.ai.dto.response.AiWeeklyReportFetchResponse;
import molip.server.ai.dto.response.AiWeeklyReportFetchResponse.WeeklyReportFetchResult;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.notification.event.ReportCreatedEvent;
import molip.server.report.entity.Report;
import molip.server.report.repository.ReportRepository;
import molip.server.report.service.ReportChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyAiReportFetchTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(WeeklyAiReportFetchTasklet.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private static final String CONTEXT_STEP4_TARGET_PAIRS = "step4TargetPairs";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_NOT_FOUND = "NOT_FOUND";

    private final AiWeeklyReportFetchClient aiWeeklyReportFetchClient;
    private final ReportRepository reportRepository;
    private final ReportChatMessageService reportChatMessageService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${batch.weekly.report-fetch.initial-delay-ms:1800000}")
    private long initialDelayMs;

    @Value("${batch.weekly.report-fetch.poll-interval-ms:1800000}")
    private long pollIntervalMs;

    @Value("${batch.weekly.report-fetch.max-attempts:10}")
    private int maxAttempts;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<Long, WeeklyReportFetchTarget> pendingTargets = loadPendingTargets(chunkContext);

        if (pendingTargets.isEmpty()) {
            log.info("Weekly AI report fetch skipped. reason=no step4 targets");
            return RepeatStatus.FINISHED;
        }

        Map<Long, Report> reportById = loadReports(pendingTargets.keySet());

        sleepSafely(initialDelayMs);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            List<WeeklyReportFetchTarget> requestTargets = new ArrayList<>(pendingTargets.values());

            AiWeeklyReportFetchResponse response =
                    aiWeeklyReportFetchClient.requestFetch(requestTargets);

            if (!response.success()) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
            }

            int successCount = handleResults(response.results(), pendingTargets, reportById);

            log.info(
                    "Weekly AI report fetch attempt finished. attempt={}/{}, requested={}, succeededNow={}, remaining={}",
                    attempt,
                    maxAttempts,
                    requestTargets.size(),
                    successCount,
                    pendingTargets.size());

            if (pendingTargets.isEmpty()) {
                return RepeatStatus.FINISHED;
            }

            if (attempt < maxAttempts) {
                sleepSafely(pollIntervalMs);
            }
        }

        throw new IllegalStateException(
                "Weekly report fetch timeout. remainingTargets=" + pendingTargets.size());
    }

    private Map<Long, WeeklyReportFetchTarget> loadPendingTargets(ChunkContext chunkContext) {
        Object raw =
                chunkContext
                        .getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext()
                        .get(CONTEXT_STEP4_TARGET_PAIRS);

        if (!(raw instanceof List<?> pairs) || pairs.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<Long, WeeklyReportFetchTarget> pending = new LinkedHashMap<>();

        for (Object item : pairs) {
            if (!(item instanceof String pair) || pair.isBlank()) {
                continue;
            }

            String[] tokens = pair.split(":");
            if (tokens.length != 2) {
                continue;
            }

            try {
                Long userId = Long.parseLong(tokens[0]);
                Long reportId = Long.parseLong(tokens[1]);

                if (userId > 0 && reportId > 0) {
                    pending.put(reportId, WeeklyReportFetchTarget.of(reportId, userId));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return pending;
    }

    private Map<Long, Report> loadReports(Iterable<Long> reportIds) {
        Map<Long, Report> reportById = new HashMap<>();

        for (Report report : reportRepository.findAllById(reportIds)) {
            reportById.put(report.getId(), report);
        }

        return reportById;
    }

    private int handleResults(
            List<WeeklyReportFetchResult> results,
            Map<Long, WeeklyReportFetchTarget> pendingTargets,
            Map<Long, Report> reportById) {

        if (results == null || results.isEmpty()) {
            return 0;
        }

        int successCount = 0;

        for (WeeklyReportFetchResult result : results) {
            if (result == null || result.reportId() == null) {
                continue;
            }

            if (!pendingTargets.containsKey(result.reportId())) {
                continue;
            }

            if (STATUS_SUCCESS.equalsIgnoreCase(result.status())) {
                Report report = reportById.get(result.reportId());

                if (report != null) {
                    boolean created =
                            reportChatMessageService.saveFirstAiSummaryMessage(
                                    report, result.content());
                    if (created) {
                        eventPublisher.publishEvent(
                                new ReportCreatedEvent(report.getUser().getId(), report.getId()));
                    }
                    successCount += created ? 1 : 0;
                    pendingTargets.remove(result.reportId());
                }

                continue;
            }

            if (STATUS_NOT_FOUND.equalsIgnoreCase(result.status())) {
                continue;
            }

            log.warn(
                    "Weekly AI report fetch unknown result status. reportId={}, userId={}, status={}",
                    result.reportId(),
                    result.userId(),
                    result.status());
        }

        return successCount;
    }

    private void sleepSafely(long millis) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Weekly report fetch sleep interrupted at " + LocalDateTime.now(ZONE_ID), e);
        }
    }
}
