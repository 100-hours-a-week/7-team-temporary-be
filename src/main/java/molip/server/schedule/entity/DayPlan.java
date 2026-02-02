package molip.server.schedule.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.user.entity.Users;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DayPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private LocalDate planDate;

    private Integer aiUsageRemainingCount;

    public DayPlan(Users user, LocalDate planDate) {
        this.user = user;
        this.planDate = planDate;
        this.aiUsageRemainingCount = 2;
    }

    public void decreaseAiUsageRemainingCount() {

        if (aiUsageRemainingCount == null) {
            aiUsageRemainingCount = 0;
            return;
        }
        aiUsageRemainingCount = Math.max(0, aiUsageRemainingCount - 1);
    }
}
