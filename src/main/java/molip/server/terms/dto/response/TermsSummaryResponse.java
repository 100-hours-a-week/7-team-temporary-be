package molip.server.terms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.enums.TermsType;
import molip.server.terms.entity.Terms;

@Schema(description = "약관 요약")
public record TermsSummaryResponse(
        @Schema(description = "약관 ID", example = "1") Long termsId,
        @Schema(description = "약관명", example = "[필수] 서비스 이용약관") String name,
        @Schema(description = "약관 타입", example = "MANDATORY") TermsType termsType) {

    public static TermsSummaryResponse from(Terms terms) {
        String displayName = terms.getTermsType().label() + " " + terms.getName();
        return new TermsSummaryResponse(terms.getId(), displayName, terms.getTermsType());
    }
}
