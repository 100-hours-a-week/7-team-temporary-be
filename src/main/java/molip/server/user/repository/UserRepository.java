package molip.server.user.repository;

import java.util.Optional;
import molip.server.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByNicknameAndDeletedAtIsNull(String nickname);

    Optional<Users> findByEmailAndDeletedAtIsNull(String email);

    Optional<Users> findByIdAndDeletedAtIsNull(Long id);

    @Query(
            "select u from Users u "
                    + "where u.deletedAt is null and u.nickname like concat(:nickname, '%')")
    Page<Users> findByNicknamePrefix(@Param("nickname") String nickname, Pageable pageable);
}
