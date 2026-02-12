package molip.server.reflection.repository;

import java.util.List;
import molip.server.reflection.entity.DayReflectionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayReflectionImageRepository extends JpaRepository<DayReflectionImage, Long> {

    @Query(
            "select dri from DayReflectionImage dri "
                    + "join fetch dri.image img "
                    + "where dri.dayReflection.id = :reflectionId "
                    + "and dri.deletedAt is null "
                    + "and img.deletedAt is null")
    List<DayReflectionImage> findByReflectionIdWithImage(@Param("reflectionId") Long reflectionId);
}
