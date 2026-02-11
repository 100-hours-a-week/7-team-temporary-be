package molip.server.schedule.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DayPlanScheduleExistResponse(
        LocalDate startDate, LocalDate endDate, List<DayPlanScheduleExistItem> days) {

    public record DayPlanScheduleExistItem(LocalDate date, boolean hasPlan) {}

    public static DayPlanScheduleExistResponse of(
            LocalDate startDate, LocalDate endDate, List<DayPlanScheduleExistItem> days) {
        return new DayPlanScheduleExistResponse(startDate, endDate, days);
    }
}
