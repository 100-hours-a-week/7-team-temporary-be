package molip.server.terms.event;

import java.util.List;
import molip.server.user.dto.request.TermsAgreementRequest;

public record UserTermsAgreedEvent(Long userId, List<TermsAgreementRequest> terms) {}
