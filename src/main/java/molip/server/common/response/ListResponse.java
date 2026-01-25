package molip.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "리스트 응답")
public record ListResponse<T>(
        @Schema(description = "리스트 콘텐츠") List<T> content,
        @Schema(description = "리스트 크기", example = "2") int size,
        @Schema(description = "전체 요소 수", example = "2") long totalElements) {}
