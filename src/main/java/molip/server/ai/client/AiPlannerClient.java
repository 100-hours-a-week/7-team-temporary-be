package molip.server.ai.client;

import lombok.extern.slf4j.Slf4j;
import molip.server.ai.dto.request.AiPlannerRequest;
import molip.server.ai.dto.response.AiPlannerResponse;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AiPlannerClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String plannerPath;

    public AiPlannerClient(
            @Qualifier("aiPlannerRestTemplate") RestTemplate restTemplate,
            @Value("${ai.planner.base-url}") String baseUrl,
            @Value("${ai.planner.path}") String plannerPath) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.plannerPath = plannerPath;
    }

    public AiPlannerResponse requestPlanner(AiPlannerRequest request) {

        try {
            String requestUrl = baseUrl + plannerPath;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            log.info("ai planner request: url={}, request={}", requestUrl, request);

            ResponseEntity<AiPlannerResponse> response =
                    restTemplate.exchange(
                            requestUrl,
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            AiPlannerResponse.class);

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error(
                    "ai planner http error: status={}, body={}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 400) {
                throw new BaseException(ErrorCode.PLANNER_BAD_REQUEST);
            }
            if (e.getStatusCode().value() == 409) {
                throw new BaseException(ErrorCode.PLANNER_CONFLICT);
            }
            if (e.getStatusCode().value() == 422) {
                throw new BaseException(ErrorCode.PLANNER_VALIDATION_ERROR);
            }
            if (e.getStatusCode().value() == 503) {
                throw new BaseException(ErrorCode.PLANNER_SERVICE_UNAVAILABLE);
            }
            throw new BaseException(ErrorCode.PLANNER_INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException e) {
            log.error("ai planner resource access error: message={}", e.getMessage(), e);
            throw new BaseException(ErrorCode.PLANNER_SERVICE_UNAVAILABLE);
        }
    }
}
