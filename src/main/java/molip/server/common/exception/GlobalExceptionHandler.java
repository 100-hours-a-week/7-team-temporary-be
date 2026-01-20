package molip.server.common.exception;

import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ServerResponse<Object>> handleCustomException(BaseException ex) {
    ErrorCode code = ex.getErrorCode();

    ex.printStackTrace();

    ServerResponse<Object> body = ServerResponse.error(code, ex.getMessage());

    return ResponseEntity.status(code.getStatus()).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ServerResponse<Object>> handleException(Exception ex) {

    ex.printStackTrace();

    ServerResponse<Object> body =
        ServerResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());

    return ResponseEntity.status(500).body(body);
  }
}
