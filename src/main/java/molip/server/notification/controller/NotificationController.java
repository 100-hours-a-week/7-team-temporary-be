package molip.server.notification.controller;

import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.notification.dto.response.NotificationItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController implements NotificationApi {

  @GetMapping("/notifications")
  @Override
  public ResponseEntity<ServerResponse<PageResponse<NotificationItemResponse>>> getNotifications(
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
  }
}
