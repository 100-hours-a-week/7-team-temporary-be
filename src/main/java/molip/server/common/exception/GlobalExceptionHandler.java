package molip.server.common.exception;

import molip.server.common.response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ServerResponse<Void>> handleCustomException(BaseException ex) {
    ErrorCode code = ex.getErrorCode();

    ex.printStackTrace();

    ServerResponse<Void> body = ServerResponse.error(code);

    return ResponseEntity.status(code.getStatus()).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ServerResponse<Void>> handleException(Exception ex) {

    ex.printStackTrace();

    ServerResponse<Void> body = ServerResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);

    return ResponseEntity.status(500).body(body);
  }
}
