package molip.server.batch.repository;

import java.util.Optional;
import molip.server.batch.entity.BatchStepRun;
import molip.server.batch.enums.BatchTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchStepRunRepository extends JpaRepository<BatchStepRun, Long> {

    Optional<BatchStepRun> findByJobRunIdAndStepNameAndTargetTypeAndTargetId(
            Long jobRunId, String stepName, BatchTargetType targetType, Long targetId);
}
