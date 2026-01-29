package molip.server.terms.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.TermsType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.terms.entity.Terms;
import molip.server.terms.entity.TermsSign;
import molip.server.terms.repository.TermsRepository;
import molip.server.terms.repository.TermsSignRepository;
import molip.server.user.dto.request.TermsAgreementRequest;
import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TermsSignCommandFacade {

    private final TermsRepository termsRepository;
    private final TermsSignRepository termsSignRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createTermsSigns(Long userId, List<TermsAgreementRequest> agreements) {

        Users user =
                userRepository
                        .findByIdAndDeletedAtIsNull(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        List<Terms> activeTerms =
                termsRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByIdAsc();

        Map<Long, Boolean> agreementMap = buildAgreementMap(agreements);

        validateAllTermsReceived(agreementMap, activeTerms);
        validateMandatoryAgree(agreementMap, activeTerms);

        if (termsSignRepository.existsByUserIdAndTermsIdInAndDeletedAtIsNull(
                userId, agreementMap.keySet().stream().toList())) {
            throw new BaseException(ErrorCode.CONFLICT_TERMS_SIGN_EXISTS);
        }

        Map<Long, Terms> termsMap = new HashMap<>();
        for (Terms terms : activeTerms) {
            termsMap.put(terms.getId(), terms);
        }

        List<TermsSign> signs = new ArrayList<>();

        for (TermsAgreementRequest request : agreements) {
            Terms terms = termsMap.get(request.termsId());

            if (terms == null) {
                throw new BaseException(ErrorCode.TERMS_NOT_FOUND);
            }

            signs.add(new TermsSign(user, terms, request.isAgreed()));
        }

        termsSignRepository.saveAll(signs);
    }

    private Map<Long, Boolean> buildAgreementMap(List<TermsAgreementRequest> agreements) {

        if (agreements == null || agreements.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        Map<Long, Boolean> agreementMap = new HashMap<>();
        for (TermsAgreementRequest agreement : agreements) {
            if (agreement == null || agreement.termsId() == null) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
            }
            if (agreementMap.containsKey(agreement.termsId())) {
                throw new BaseException(ErrorCode.CONFLICT_TERMS_SIGN_EXISTS);
            }
            agreementMap.put(agreement.termsId(), agreement.isAgreed());
        }

        return agreementMap;
    }

    private void validateAllTermsReceived(
            Map<Long, Boolean> agreementMap, List<Terms> activeTerms) {

        if (agreementMap.size() != activeTerms.size()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        for (Terms terms : activeTerms) {
            if (!agreementMap.containsKey(terms.getId())) {
                throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
            }
        }
    }

    private void validateMandatoryAgree(Map<Long, Boolean> agreementMap, List<Terms> activeTerms) {

        int agreedMandatoryCount = 0;
        int mandatoryCount = 0;

        for (Terms terms : activeTerms) {
            if (terms.getTermsType() == TermsType.MANDATORY) {
                mandatoryCount++;

                if (Boolean.TRUE.equals(agreementMap.get(terms.getId()))) {
                    agreedMandatoryCount++;
                }
            }
        }

        if (agreedMandatoryCount != mandatoryCount) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MANDATORY_TERMS_REQUIRED);
        }
    }
}
