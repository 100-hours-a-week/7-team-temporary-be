package molip.server.schedule.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTimeParser {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public LocalTime parseOrThrow(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_TIME_FORMAT);
        }
    }
}
