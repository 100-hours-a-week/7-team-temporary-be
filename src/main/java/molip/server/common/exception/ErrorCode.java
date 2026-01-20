package molip.server.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  // 공통
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error"),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid_request"),

  // 인증 관련
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "not_login_user"),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "token_expired"),
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "invalid_token"),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "invalid_password"),

  // 유저
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "not_found_user"),
  NOT_LOGIN_USER(HttpStatus.UNAUTHORIZED, "not_login_user"),
  ALREADY_DELETED_USER(HttpStatus.NOT_FOUND, "already_deleted_user"),
  ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "already_exist_email");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
