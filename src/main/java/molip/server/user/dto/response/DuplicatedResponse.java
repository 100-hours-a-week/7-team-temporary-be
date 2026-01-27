package molip.server.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "중복 여부 응답")
public record DuplicatedResponse(
        @Schema(description = "중복 여부", example = "false") boolean isDuplicated) {

    public static DuplicatedResponse from(boolean isDuplicated) {
        return new DuplicatedResponse(isDuplicated);
    }
}
