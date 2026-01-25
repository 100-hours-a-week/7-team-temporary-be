package molip.server.terms.repository;

import java.util.Optional;
import molip.server.terms.entity.TermsSign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsSignRepository extends JpaRepository<TermsSign, Long> {
    boolean existsByUserIdAndTermsIdAndDeletedAtIsNull(Long userId, Long termsId);

    Optional<TermsSign> findByIdAndDeletedAtIsNull(Long id);
}
