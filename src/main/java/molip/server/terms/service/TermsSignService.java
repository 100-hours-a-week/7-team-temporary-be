package molip.server.terms.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.enums.TermsType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.terms.entity.TermsSign;
import molip.server.terms.repository.TermsSignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsSignService {
    private final TermsSignRepository termsSignRepository;

    @Transactional
    public void updateTermsSign(Long userId, Long termsId, boolean isAgreed) {
        TermsSign termsSign =
                termsSignRepository
                        .findByUserIdAndTermsIdWithTerms(userId, termsId)
                        .orElseThrow(() -> new BaseException(ErrorCode.TERMS_SIGN_NOT_FOUND));

        if (termsSign.getTerms().getTermsType() == TermsType.MANDATORY && !isAgreed) {
            throw new BaseException(ErrorCode.CONFLICT_TERMS_WITHDRAW_NOT_ALLOWED);
        }

        termsSign.updateAgreement(isAgreed);
    }
}
