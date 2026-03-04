package molip.server.report.repository;

import java.util.List;
import molip.server.common.enums.SenderType;
import molip.server.report.entity.ReportChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportChatMessageRepository extends JpaRepository<ReportChatMessage, Long> {

    boolean existsByIdAndReportIdAndDeletedAtIsNullAndIsDeletedFalse(Long messageId, Long reportId);

    Page<ReportChatMessage> findByReportIdAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
            Long reportId, Pageable pageable);

    Page<ReportChatMessage>
            findByReportIdAndDeletedAtIsNullAndIsDeletedFalseAndIdLessThanOrderByIdDesc(
                    Long reportId, Long cursor, Pageable pageable);

    boolean existsByReportIdAndSenderTypeAndContentIsNullAndDeletedAtIsNullAndIsDeletedFalse(
            Long reportId, SenderType senderType);

    List<ReportChatMessage>
            findByReportIdAndDeletedAtIsNullAndIsDeletedFalseAndContentIsNotNullOrderByIdAsc(
                    Long reportId);
}
