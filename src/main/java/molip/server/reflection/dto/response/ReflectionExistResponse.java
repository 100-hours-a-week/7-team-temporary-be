package molip.server.reflection.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회고 작성 여부 응답")
public record ReflectionExistResponse(
        @Schema(description = "작성 여부", example = "true") boolean alreadyWrote) {}
