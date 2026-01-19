package molip.server.terms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 동의 생성 응답")
public record TermsSignResponse(
    @Schema(description = "약관 동의 ID", example = "15") Long termsSignId) {}
