package molip.server.schedule.entity;

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
import molip.server.schedule.enums.ScheduleActionType;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleActionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    private ScheduleActionType actionType;

    private String apiPath;

    public ScheduleActionLog(
            Long userId, Long scheduleId, ScheduleActionType actionType, String apiPath) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.actionType = actionType;
        this.apiPath = apiPath;
    }
}
