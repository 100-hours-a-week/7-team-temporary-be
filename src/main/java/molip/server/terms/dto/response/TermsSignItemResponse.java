package molip.server.terms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import molip.server.common.enums.TermsType;

@Schema(description = "약관 동의 항목")
public record TermsSignItemResponse(
        @Schema(description = "약관 동의 ID", example = "15") Long termsSignId,
        @Schema(description = "약관 ID", example = "1") Long termsId,
        @Schema(description = "약관명", example = "서비스 이용약관") String name,
        @Schema(description = "약관 타입", example = "MANDATORY") TermsType termsType,
        @Schema(description = "동의 여부", example = "true") boolean isAgreed,
        @Schema(description = "동의 시각", example = "2026-01-13T10:10:10+09:00")
                OffsetDateTime agreedAt) {}
