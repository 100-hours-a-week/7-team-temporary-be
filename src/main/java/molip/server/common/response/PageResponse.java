package molip.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이지네이션 응답")
public record PageResponse<T>(
    @Schema(description = "페이지 콘텐츠") List<T> content,
    @Schema(description = "현재 페이지", example = "1") int page,
    @Schema(description = "페이지 크기", example = "10") int size,
    @Schema(description = "전체 요소 수", example = "25") long totalElements,
    @Schema(description = "전체 페이지 수", example = "3") int totalPages) {}
