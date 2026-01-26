package molip.server.terms.repository;

import java.util.Optional;
import molip.server.terms.entity.TermsSign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TermsSignRepository extends JpaRepository<TermsSign, Long> {
    boolean existsByUserIdAndTermsIdAndDeletedAtIsNull(Long userId, Long termsId);

    @Query(
            "select ts from TermsSign ts "
                    + "join fetch ts.terms t "
                    + "where ts.user.id = :userId and t.id = :termsId and ts.deletedAt is null")
    Optional<TermsSign> findByUserIdAndTermsIdWithTerms(
            @Param("userId") Long userId, @Param("termsId") Long termsId);
}
