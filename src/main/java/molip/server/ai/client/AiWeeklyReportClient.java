package molip.server.ai.client;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import molip.server.ai.dto.request.AiWeeklyReportGenerateRequest;
import molip.server.ai.dto.request.AiWeeklyReportGenerateRequest.UserReportTarget;
import molip.server.ai.dto.response.AiWeeklyReportGenerateResponse;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AiWeeklyReportClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String weeklyGeneratePath;

    public AiWeeklyReportClient(
            @Qualifier("aiWeeklyReportRestTemplate") RestTemplate restTemplate,
            @Value("${ai.planner.base-url}") String baseUrl,
            @Value("${ai.report.weekly-generate-path}") String weeklyGeneratePath) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.weeklyGeneratePath = weeklyGeneratePath;
    }

    public AiWeeklyReportGenerateResponse requestGenerate(
            LocalDate baseDate, List<UserReportTarget> targets) {
        AiWeeklyReportGenerateRequest request = AiWeeklyReportGenerateRequest.of(baseDate, targets);

        try {
            log.info(
                    "ai weekly report generate request: baseDate={}, targetCount={}",
                    baseDate,
                    targets == null ? 0 : targets.size());

            ResponseEntity<AiWeeklyReportGenerateResponse> response =
                    restTemplate.exchange(
                            baseUrl + weeklyGeneratePath,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            AiWeeklyReportGenerateResponse.class);

            AiWeeklyReportGenerateResponse body = response.getBody();

            if (body == null) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
            }

            return body;
        } catch (HttpStatusCodeException e) {
            int statusCode = e.getStatusCode().value();

            log.error(
                    "ai weekly report generate http error: baseDate={}, status={}, body={}",
                    baseDate,
                    statusCode,
                    e.getResponseBodyAsString());

            if (statusCode == 400) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_BAD_REQUEST);
            }

            if (statusCode == 404) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_DATA_NOT_FOUND);
            }

            if (statusCode == 409) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_CONFLICT);
            }

            if (statusCode == 422) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_VALIDATION_ERROR);
            }

            if (statusCode == 503) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_SERVICE_UNAVAILABLE);
            }

            throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            throw new BaseException(ErrorCode.WEEKLY_REPORT_SERVICE_UNAVAILABLE);
        }
    }
}
