package molip.server.reflection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.schedule.entity.DayPlan;
import molip.server.user.entity.Users;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DayReflection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_plan_id", nullable = false)
    private DayPlan dayPlan;

    @Column(nullable = false, length = 13)
    private String title;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(name = "is_open", nullable = false)
    private boolean isOpen;

    public DayReflection(
            Users user, DayPlan dayPlan, String title, String content, boolean isOpen) {
        this.user = user;
        this.dayPlan = dayPlan;
        this.title = title;
        this.content = content;
        this.isOpen = isOpen;
    }

    public void updateOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
