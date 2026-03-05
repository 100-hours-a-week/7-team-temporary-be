package molip.server.batch.weeklyIngest.step3;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiPersonalizationClient;
import molip.server.ai.dto.response.AiPersonalizationIngestResponse;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyAiNotifyTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(WeeklyAiNotifyTasklet.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int PAGE_SIZE = 1000;

    private final UserRepository userRepository;
    private final AiPersonalizationClient aiPersonalizationClient;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate runDate = resolveRunDate(chunkContext);
        List<Long> userIds = collectActiveUserIds();

        if (userIds.isEmpty()) {
            log.info("Weekly AI notify skipped. targetDate={}, reason=no active users", runDate);
            return RepeatStatus.FINISHED;
        }

        AiPersonalizationIngestResponse response =
                aiPersonalizationClient.requestIngest(userIds, runDate);

        if (!response.success()) {
            throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_INTERNAL_SERVER_ERROR);
        }

        log.info(
                "Weekly AI notify succeeded. targetDate={}, requestedUsers={}, acknowledgedUsers={}",
                runDate,
                userIds.size(),
                response.userIds() == null ? 0 : response.userIds().size());

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

    private List<Long> collectActiveUserIds() {
        List<Long> userIds = new ArrayList<>();
        int page = 0;

        while (true) {
            Page<Users> result =
                    userRepository.findByDeletedAtIsNull(PageRequest.of(page, PAGE_SIZE));

            if (result.isEmpty()) {
                break;
            }

            result.getContent().stream().map(Users::getId).forEach(userIds::add);

            if (!result.hasNext()) {
                break;
            }

            page += 1;
        }

        return userIds;
    }
}
