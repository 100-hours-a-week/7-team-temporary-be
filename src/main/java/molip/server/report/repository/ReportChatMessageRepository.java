package molip.server.report.repository;

import molip.server.report.entity.ReportChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportChatMessageRepository extends JpaRepository<ReportChatMessage, Long> {

    boolean existsByIdAndReportIdAndDeletedAtIsNullAndIsDeletedFalse(Long messageId, Long reportId);

    boolean existsByReportIdAndDeletedAtIsNullAndIsDeletedFalse(Long reportId);

    Page<ReportChatMessage> findByReportIdAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
            Long reportId, Pageable pageable);

    Page<ReportChatMessage>
            findByReportIdAndDeletedAtIsNullAndIsDeletedFalseAndIdLessThanOrderByIdDesc(
                    Long reportId, Long cursor, Pageable pageable);
}
