package molip.server.schedule.service;

import lombok.RequiredArgsConstructor;
import molip.server.schedule.entity.ScheduleHistory;
import molip.server.schedule.repository.ScheduleHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleHistoryService {

    private final ScheduleHistoryRepository scheduleHistoryRepository;

    @Transactional
    public void saveHistory(ScheduleHistory history) {

        scheduleHistoryRepository.save(history);
    }
}
