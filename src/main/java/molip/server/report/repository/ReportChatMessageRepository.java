package molip.server.report.repository;

import java.util.List;
import java.util.Optional;
import molip.server.common.enums.SenderType;
import molip.server.report.entity.ReportChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportChatMessageRepository extends JpaRepository<ReportChatMessage, Long> {

    boolean existsByIdAndReportId(Long messageId, Long reportId);

    Optional<ReportChatMessage> findByIdAndReportId(Long messageId, Long reportId);

    Optional<ReportChatMessage> findTopByReportIdAndSenderTypeAndIdLessThanOrderByIdDesc(
            Long reportId, SenderType senderType, Long streamMessageId);

    Optional<ReportChatMessage>
            findTopByReportIdAndSenderTypeAndContentIsNullAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
                    Long reportId, SenderType senderType);

    Optional<ReportChatMessage>
            findTopByReportIdAndSenderTypeAndIdLessThanAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
                    Long reportId, SenderType senderType, Long streamMessageId);

    boolean existsByIdAndReportIdAndDeletedAtIsNullAndIsDeletedFalse(Long messageId, Long reportId);

    boolean existsByReportIdAndDeletedAtIsNullAndIsDeletedFalse(Long reportId);

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

    java.util.Optional<ReportChatMessage> findByIdAndDeletedAtIsNullAndIsDeletedFalse(Long id);
}
