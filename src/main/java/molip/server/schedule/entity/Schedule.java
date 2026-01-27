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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import molip.server.common.entity.BaseEntity;
import molip.server.common.enums.AssignedBy;
import molip.server.common.enums.AssignmentStatus;
import molip.server.common.enums.EstimatedTimeRange;
import molip.server.common.enums.ScheduleStatus;
import molip.server.common.enums.ScheduleType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "day_plan_id", nullable = false)
    private DayPlan dayPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_schedule_id")
    private Schedule parentSchedule;

    private String title;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    @Enumerated(EnumType.STRING)
    private AssignedBy assignedBy;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus assignmentStatus;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private EstimatedTimeRange estimatedTimeRange;

    private Integer focusLevel;

    private Boolean isUrgent;

    @Builder
    public Schedule(
            DayPlan dayPlan,
            Schedule parentSchedule,
            String title,
            ScheduleStatus status,
            ScheduleType type,
            AssignedBy assignedBy,
            AssignmentStatus assignmentStatus,
            LocalDateTime startAt,
            LocalDateTime endAt,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {
        this.dayPlan = dayPlan;
        this.parentSchedule = parentSchedule;
        this.title = title;
        this.status = status;
        this.type = type;
        this.assignedBy = assignedBy;
        this.assignmentStatus = assignmentStatus;
        this.startAt = startAt;
        this.endAt = endAt;
        this.estimatedTimeRange = estimatedTimeRange;
        this.focusLevel = focusLevel;
        this.isUrgent = isUrgent;
    }

    public void updateAsFixed(String title, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.type = ScheduleType.FIXED;
        this.assignedBy = AssignedBy.USER;
        this.assignmentStatus = AssignmentStatus.FIXED;
        this.startAt = startAt;
        this.endAt = endAt;
        this.estimatedTimeRange = null;
        this.focusLevel = null;
        this.isUrgent = null;
    }

    public void updateAsFlex(
            String title,
            EstimatedTimeRange estimatedTimeRange,
            Integer focusLevel,
            Boolean isUrgent) {
        this.title = title;
        this.type = ScheduleType.FLEX;
        this.assignedBy = AssignedBy.USER;
        this.assignmentStatus = AssignmentStatus.NOT_ASSIGNED;
        this.startAt = null;
        this.endAt = null;
        this.estimatedTimeRange = estimatedTimeRange;
        this.focusLevel = focusLevel;
        this.isUrgent = isUrgent;
    }

    public void updateStatus(ScheduleStatus status) {
        this.status = status;
    }
}
