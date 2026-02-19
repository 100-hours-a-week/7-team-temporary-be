package molip.server.reflection.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.image.entity.Image;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DayReflectionImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_reflection_id", nullable = false)
    private DayReflection dayReflection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    public DayReflectionImage(DayReflection dayReflection, Image image) {
        this.dayReflection = dayReflection;
        this.image = image;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
