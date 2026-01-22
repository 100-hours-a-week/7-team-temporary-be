package molip.server.reflection.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "회고 생성 요청")
public record ReflectionCreateRequest(
        @Schema(description = "회고 이미지 ID 목록", example = "[2,3,6]") List<Long> reflectionImageIds,
        @Schema(description = "내용", example = "this is reflection") String content) {}
