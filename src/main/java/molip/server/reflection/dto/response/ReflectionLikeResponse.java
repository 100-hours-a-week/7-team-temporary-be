package molip.server.reflection.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회고 좋아요 여부")
public record ReflectionLikeResponse(
    @Schema(description = "좋아요 여부", example = "true") boolean liked) {}
