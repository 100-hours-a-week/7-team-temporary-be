package molip.server.ai.client;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            log.info(
                    "ai report respond request: reportId={}, messageId={}, userId={}, messageCount={}",
                    reportId,
                    request.messageId(),
                    request.userId(),
                    request.messages() == null ? 0 : request.messages().size());

            ResponseEntity<AiReportChatRespondResponse> response =
                    restTemplate.exchange(
                            baseUrl + respondPath,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            AiReportChatRespondResponse.class,
                            Map.of("reportId", reportId));

            AiReportChatRespondResponse body = response.getBody();

            if (body == null
                    || !body.success()
                    || body.data() == null
                    || body.data().messageId() == null) {
                log.warn(
                        "ai report respond invalid body: reportId={}, messageId={}, body={}",
                        reportId,
                        request.messageId(),
                        body);
                throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            log.info(
                    "ai report respond success: reportId={}, requestedMessageId={}, streamMessageId={}",
                    reportId,
                    request.messageId(),
                    body.data().messageId());

            return body;
        } catch (HttpStatusCodeException exception) {
            log.error(
                    "ai report respond http error: reportId={}, messageId={}, status={}, body={}",
                    reportId,
                    request.messageId(),
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString());

            if (exception.getStatusCode().value() == 409) {
                throw new BaseException(ErrorCode.CONFLICT_REPORT_RESPONSE_RUNNING);
            }

            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException exception) {
            log.error(
                    "ai report respond resource access error: reportId={}, messageId={}, message={}",
                    reportId,
                    request.messageId(),
                    exception.getMessage(),
                    exception);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void requestCancel(Long reportId, Long streamMessageId) {
        try {
            log.info(
                    "ai report cancel request: reportId={}, streamMessageId={}",
                    reportId,
                    streamMessageId);

            restTemplate.exchange(
                    baseUrl + cancelPath,
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    Map.of(
                            "reportId", reportId,
                            "messageId", streamMessageId,
                            "streamMessageId", streamMessageId));

            log.info(
                    "ai report cancel success: reportId={}, streamMessageId={}",
                    reportId,
                    streamMessageId);
        } catch (HttpStatusCodeException exception) {
            log.error(
                    "ai report cancel http error: reportId={}, streamMessageId={}, status={}, body={}",
                    reportId,
                    streamMessageId,
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString());

            if (exception.getStatusCode().value() == 404) {
                throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
            }

            if (exception.getStatusCode().value() == 409) {
                throw new BaseException(ErrorCode.CONFLICT_RESPONSE_ALREADY_ENDED);
            }

            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (ResourceAccessException exception) {
            log.error(
                    "ai report cancel resource access error: reportId={}, streamMessageId={}, message={}",
                    reportId,
                    streamMessageId,
                    exception.getMessage(),
                    exception);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
