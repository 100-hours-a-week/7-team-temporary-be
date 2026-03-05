package molip.server.ai.client;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import molip.server.ai.dto.request.AiWeeklyReportFetchRequest;
import molip.server.ai.dto.request.AiWeeklyReportFetchRequest.WeeklyReportFetchTarget;
import molip.server.ai.dto.response.AiWeeklyReportFetchResponse;
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
public class AiWeeklyReportFetchClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String weeklyFetchPath;

    public AiWeeklyReportFetchClient(
            @Qualifier("aiWeeklyReportRestTemplate") RestTemplate restTemplate,
            @Value("${ai.planner.base-url}") String baseUrl,
            @Value("${ai.report.weekly-fetch-path}") String weeklyFetchPath) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.weeklyFetchPath = weeklyFetchPath;
    }

    public AiWeeklyReportFetchResponse requestFetch(List<WeeklyReportFetchTarget> targets) {
        AiWeeklyReportFetchRequest request = AiWeeklyReportFetchRequest.of(targets);

        try {
            log.info(
                    "ai weekly report fetch request: targetCount={}",
                    targets == null ? 0 : targets.size());

            ResponseEntity<AiWeeklyReportFetchResponse> response =
                    restTemplate.exchange(
                            baseUrl + weeklyFetchPath,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            AiWeeklyReportFetchResponse.class);

            AiWeeklyReportFetchResponse body = response.getBody();
            if (body == null) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
            }

            return body;
        } catch (HttpStatusCodeException e) {
            int statusCode = e.getStatusCode().value();

            log.error(
                    "ai weekly report fetch http error: status={}, body={}",
                    statusCode,
                    e.getResponseBodyAsString());

            if (statusCode == 400) {
                throw new BaseException(ErrorCode.WEEKLY_REPORT_FETCH_BAD_REQUEST);
            }

            throw new BaseException(ErrorCode.WEEKLY_REPORT_INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            throw new BaseException(ErrorCode.WEEKLY_REPORT_SERVICE_UNAVAILABLE);
        }
    }
}
