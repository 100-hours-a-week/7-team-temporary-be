package molip.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import molip.server.common.SuccessCode;
import molip.server.common.exception.ErrorCode;

@Schema(description = "API response wrapper")
public record ServerResponse<T>(
    @Schema(description = "응답 상태", example = "SUCCESS") String status,
    @Schema(description = "응답 메시지", example = "요청이 성공했습니다.") String message,
    @Schema(description = "응답 데이터") T data) {

  public static <T> ServerResponse<T> success(SuccessCode successCode, T data) {
    return new ServerResponse<>(successCode.getStatusValue(), successCode.getMessage(), data);
  }

  public static ServerResponse<Void> error(ErrorCode errorCode) {
    return new ServerResponse<>(errorCode.getStatusValue(), errorCode.getMessage(), null);
  }
}
