package molip.server.schedule.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import molip.server.common.enums.ScheduleHistoryEventType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ScheduleHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    private ScheduleHistoryEventType eventType;

    private LocalDateTime prevStartAt;

    private LocalDateTime prevEndAt;

    private LocalDateTime nextStartAt;

    private LocalDateTime nextEndAt;

    public ScheduleHistory(
            Schedule schedule,
            ScheduleHistoryEventType eventType,
            LocalDateTime prevStartAt,
            LocalDateTime prevEndAt,
            LocalDateTime nextStartAt,
            LocalDateTime nextEndAt) {

        this.schedule = schedule;
        this.eventType = eventType;
        this.prevStartAt = prevStartAt;
        this.prevEndAt = prevEndAt;
        this.nextStartAt = nextStartAt;
        this.nextEndAt = nextEndAt;
    }

    public void deleteHistory() {

        this.deletedAt = LocalDateTime.now();
    }
}
