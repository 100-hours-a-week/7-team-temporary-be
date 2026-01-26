package molip.server.terms.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.terms.dto.response.TermsItemResponse;
import molip.server.terms.dto.response.TermsSignResponse;
import molip.server.terms.entity.Terms;
import molip.server.terms.entity.TermsSign;
import molip.server.terms.repository.TermsRepository;
import molip.server.terms.repository.TermsSignRepository;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsRepository termsRepository;
    private final TermsSignRepository termsSignRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TermsItemResponse> getActiveTerms() {
        return termsRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                .map(TermsItemResponse::from)
                .toList();
    }

    @Transactional
    public TermsSignResponse createTermsSign(Long userId, Long termsId, boolean isAgreed) {
        Users user =
                userRepository
                        .findByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Terms terms =
                termsRepository
                        .findByIdAndDeletedAtIsNull(termsId)
                        .orElseThrow(() -> new BaseException(ErrorCode.TERMS_NOT_FOUND));

        if (termsSignRepository.existsByUserIdAndTermsIdAndDeletedAtIsNull(userId, termsId)) {
            throw new BaseException(ErrorCode.CONFLICT_TERMS_SIGN_EXISTS);
        }

        TermsSign saved = termsSignRepository.save(new TermsSign(user, terms, isAgreed));
        return new TermsSignResponse(saved.getId());
    }
}
