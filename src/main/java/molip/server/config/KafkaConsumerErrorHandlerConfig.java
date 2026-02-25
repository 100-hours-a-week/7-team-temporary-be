package molip.server.config;

import java.sql.SQLIntegrityConstraintViolationException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerErrorHandlerConfig {

    @Bean
    public CommonErrorHandler kafkaConsumerErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka.consumer.dlq.max-attempts:10}") long maxAttempts,
            @Value("${kafka.consumer.dlq.backoff-ms:0}") long backoffMs) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) ->
                                new TopicPartition(record.topic() + ".DLQ", record.partition()));
        FixedBackOff backOff = new FixedBackOff(backoffMs, Math.max(0, maxAttempts - 1));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(
                DataIntegrityViolationException.class,
                SQLIntegrityConstraintViolationException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}
