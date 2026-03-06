package molip.server.batch.weeklyIngest.step1;

import java.util.Map;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;

public class WeeklyScoreUserReader extends RepositoryItemReader<Users> {

    public WeeklyScoreUserReader(UserRepository userRepository) {
        super(userRepository, Map.of("id", Sort.Direction.ASC));
        setMethodName("findByDeletedAtIsNull");
        setPageSize(1000);
    }
}
