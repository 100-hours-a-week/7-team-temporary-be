package molip.server.ai.client;

import java.time.LocalDate;
import java.util.List;
import molip.server.ai.dto.request.AiPersonalizationIngestRequest;
import molip.server.ai.dto.response.AiPersonalizationIngestResponse;
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
public class AiPersonalizationClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String personalizationPath;

    public AiPersonalizationClient(
            @Qualifier("aiPlannerRestTemplate") RestTemplate restTemplate,
            @Value("${ai.planner.base-url}") String baseUrl,
            @Value("${ai.personalization.path}") String personalizationPath) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.personalizationPath = personalizationPath;
    }

    public AiPersonalizationIngestResponse requestIngest(List<Long> userIds, LocalDate targetDate) {
        AiPersonalizationIngestRequest request =
                AiPersonalizationIngestRequest.of(userIds, targetDate);

        try {
            ResponseEntity<AiPersonalizationIngestResponse> response =
                    restTemplate.exchange(
                            baseUrl + personalizationPath,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            AiPersonalizationIngestResponse.class);

            AiPersonalizationIngestResponse body = response.getBody();

            if (body == null) {
                throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_INTERNAL_SERVER_ERROR);
            }

            return body;
        } catch (HttpStatusCodeException e) {
            int statusCode = e.getStatusCode().value();

            if (statusCode == 400) {
                throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_BAD_REQUEST);
            }

            if (statusCode == 409) {
                throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_CONFLICT);
            }

            if (statusCode == 422) {
                throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_VALIDATION_ERROR);
            }

            if (statusCode == 503) {
                throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_SERVICE_UNAVAILABLE);
            }

            throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            throw new BaseException(ErrorCode.PERSONALIZATION_INGEST_SERVICE_UNAVAILABLE);
        }
    }
}
