package molip.server.reflection.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "회고 수정 요청")
public record ReflectionUpdateRequest(
        @Schema(description = "회고 이미지 Key 목록", example = "[\"a1b2c3\",\"d4e5f6\"]")
                List<String> reflectionImageKeys,
        @Schema(description = "내용", example = "this is reflection") String content,
        @Schema(description = "공개 여부", example = "true") Boolean isOpen) {}
