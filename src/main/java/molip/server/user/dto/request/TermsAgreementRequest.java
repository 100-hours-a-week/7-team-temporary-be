package molip.server.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 동의 항목")
public record TermsAgreementRequest(
        @Schema(description = "약관 ID", example = "1") Long termsId,
        @Schema(description = "동의 여부", example = "true") boolean isAgreed) {}
