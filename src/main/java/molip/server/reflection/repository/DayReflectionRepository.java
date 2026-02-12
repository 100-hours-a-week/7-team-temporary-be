package molip.server.reflection.repository;

import java.util.Optional;
import molip.server.reflection.entity.DayReflection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayReflectionRepository extends JpaRepository<DayReflection, Long> {

    boolean existsByDayPlanIdAndDeletedAtIsNull(Long dayPlanId);

    Optional<DayReflection> findByIdAndIsOpenTrueAndDeletedAtIsNull(Long reflectionId);

    Optional<DayReflection> findByIdAndDeletedAtIsNull(Long reflectionId);

    @Query(
            value =
                    "select count(*) from reflection_like "
                            + "where day_reflection_id = :reflectionId and deleted_at is null",
            nativeQuery = true)
    long countLikes(@Param("reflectionId") Long reflectionId);
}
