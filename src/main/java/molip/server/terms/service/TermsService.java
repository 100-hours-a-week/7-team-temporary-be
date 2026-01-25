package molip.server.terms.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.terms.dto.response.TermsItemResponse;
import molip.server.terms.repository.TermsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsRepository termsRepository;

    @Transactional(readOnly = true)
    public List<TermsItemResponse> getActiveTerms() {
        return termsRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                .map(TermsItemResponse::from)
                .toList();
    }
}
