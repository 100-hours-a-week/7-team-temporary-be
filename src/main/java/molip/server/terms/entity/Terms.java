package molip.server.terms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.common.enums.TermsType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermsType termsType;

    @Column(nullable = false)
    private boolean isActive;

    public Terms(String name, TermsType termsType, boolean isActive) {
        this.name = name;
        this.termsType = termsType;
        this.isActive = isActive;
    }
}
