package molip.server.batch.repository;

import java.time.LocalDate;
import java.util.Optional;
import molip.server.batch.entity.BatchJobRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobRunRepository extends JpaRepository<BatchJobRun, Long> {

    Optional<BatchJobRun> findByJobNameAndRunDate(String jobName, LocalDate runDate);
}
