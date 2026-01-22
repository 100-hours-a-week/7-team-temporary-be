package molip.server.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 동의 요청")
public record TermsSignRequest(
        @Schema(description = "동의 여부", example = "false") boolean isAgreed) {}
