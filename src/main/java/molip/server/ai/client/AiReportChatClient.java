package molip.server.ai.client;

import molip.server.ai.dto.request.AiReportChatRespondRequest;
import molip.server.ai.dto.response.AiReportChatRespondResponse;
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
public class AiReportChatClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String respondPath;
    private final String cancelPath;

    public AiReportChatClient(
            @Qualifier("aiReportChatRespondRestTemplate") RestTemplate restTemplate,
            @Value("${ai.chatbot.base-url}") String baseUrl,
            @Value("${ai.chatbot.respond-path}") String respondPath,
            @Value("${ai.chatbot.cancel-path}") String cancelPath) {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.respondPath = respondPath;
        this.cancelPath = cancelPath;
    }

    public AiReportChatRespondResponse requestRespond(
            Long reportId, AiReportChatRespondRequest request) {
        try {
            ResponseEntity<AiReportChatRespondResponse> response =
                    restTemplate.exchange(
                            baseUrl + respondPath,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            AiReportChatRespondResponse.class,
                            reportId);

            AiReportChatRespondResponse body = response.getBody();

            if (body == null
                    || !body.success()
                    || body.data() == null
                    || body.data().messageId() == null) {
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            return body;
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new BaseException(ErrorCode.CONFLICT_REPORT_RESPONSE_RUNNING);
            }

            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void requestCancel(Long reportId, Long streamMessageId) {
        try {
            restTemplate.exchange(
                    baseUrl + cancelPath,
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    reportId,
                    streamMessageId);
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
            }

            if (exception.getStatusCode().value() == 409) {
                throw new BaseException(ErrorCode.CONFLICT_RESPONSE_ALREADY_ENDED);
            }

            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException exception) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
