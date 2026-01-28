package molip.server.schedule.service.strategy;

import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTimeParser {
    public LocalTime parse(LocalTime time) {
        return time;
    }
}
