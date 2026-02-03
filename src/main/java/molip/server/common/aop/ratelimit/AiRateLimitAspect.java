package molip.server.common.aop.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AiRateLimitAspect {

    private static final long LIMIT_PER_MINUTE = 40L;

    private final Bucket bucket =
            Bucket.builder()
                    .addLimit(Bandwidth.simple(LIMIT_PER_MINUTE, Duration.ofMinutes(1)))
                    .build();

    @Before("@annotation(molip.server.common.aop.ratelimit.AiRateLimit)")
    public void limit() {
        if (!bucket.tryConsume(1)) {
            throw new BaseException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }
}
