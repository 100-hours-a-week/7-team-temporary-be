package molip.server.user.repository;

import java.util.Optional;
import molip.server.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<Users> findByEmailAndDeletedAtIsNull(String email);

    Optional<Users> findByIdAndDeletedAtIsNull(Long id);
}
