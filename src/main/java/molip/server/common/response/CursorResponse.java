package molip.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 페이지네이션 응답")
public record CursorResponse<T>(
        @Schema(description = "페이지 콘텐츠") List<T> content,
        @Schema(description = "다음 커서", example = "160") Long nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext,
        @Schema(description = "요청한 크기", example = "20") int size) {}
