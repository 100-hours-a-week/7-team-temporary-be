package molip.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API response wrapper")
public record ServerResponse<T>(
    @Schema(description = "응답 상태", example = "SUCCESS") String status,
    @Schema(description = "응답 메시지", example = "요청이 성공했습니다.") String message,
    @Schema(description = "응답 데이터") T data) {}
