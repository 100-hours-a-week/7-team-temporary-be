package molip.server.reflection.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회고 생성 응답")
public record ReflectionCreateResponse(
    @Schema(description = "회고 ID", example = "55") Long reflectionId) {}
