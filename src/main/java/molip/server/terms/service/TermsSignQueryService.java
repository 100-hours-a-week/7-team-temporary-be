package molip.server.terms.service;

import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.terms.dto.response.TermsSignHistoryResponse;
import molip.server.terms.repository.TermsSignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsSignQueryService {
    private final TermsSignRepository termsSignRepository;

    @Transactional(readOnly = true)
    public List<TermsSignHistoryResponse> getMyTermsSigns(Long userId) {
        return termsSignRepository.findByUserIdAndDeletedAtIsNullOrderByIdDesc(userId).stream()
                .map(
                        termsSign ->
                                new TermsSignHistoryResponse(
                                        termsSign.getId(),
                                        termsSign.getTerms().getId(),
                                        termsSign.getTerms().getName(),
                                        termsSign.getTerms().getTermsType(),
                                        termsSign.isAgreed(),
                                        termsSign
                                                .getUpdatedAt()
                                                .atZone(ZoneId.of("Asia/Seoul"))
                                                .toOffsetDateTime()))
                .toList();
    }
}
