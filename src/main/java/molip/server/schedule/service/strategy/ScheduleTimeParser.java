package molip.server.schedule.service.strategy;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTimeParser {
    public LocalDateTime parse(LocalDateTime time) {
        return time;
    }
}
