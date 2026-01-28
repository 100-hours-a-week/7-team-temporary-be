package molip.server.terms.repository;

import java.util.List;
import java.util.Optional;
import molip.server.common.enums.TermsType;
import molip.server.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    List<Terms> findByIsActiveTrueAndDeletedAtIsNullOrderByIdAsc();

    Optional<Terms> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long id);

    List<Terms> findByTermsTypeAndIsActiveTrueAndDeletedAtIsNull(TermsType termsType);

    List<Terms> findByIdInAndIsActiveTrueAndDeletedAtIsNull(List<Long> ids);
}
