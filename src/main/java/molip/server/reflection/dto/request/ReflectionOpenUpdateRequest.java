package molip.server.reflection.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회고 공개 여부 수정 요청")
public record ReflectionOpenUpdateRequest(
        @Schema(description = "공개 여부", example = "true") Boolean isOpen) {}
