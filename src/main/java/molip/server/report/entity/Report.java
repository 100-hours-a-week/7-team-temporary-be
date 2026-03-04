package molip.server.report.entity;

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
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private LocalDate startDate;

    private LocalDate endDate;

    private int aiReportResponseLimit;

    private int aiReportResponseUsed;

    public Report(
            Users user,
            LocalDate startDate,
            LocalDate endDate,
            int aiReportResponseLimit,
            int aiReportResponseUsed) {

        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.aiReportResponseLimit = aiReportResponseLimit;
        this.aiReportResponseUsed = aiReportResponseUsed;
    }
}
