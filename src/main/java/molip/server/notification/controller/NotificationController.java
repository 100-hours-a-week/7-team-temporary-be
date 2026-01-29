package molip.server.notification.controller;

import lombok.RequiredArgsConstructor;
import molip.server.common.SuccessCode;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.notification.dto.response.NotificationItemResponse;
import molip.server.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<NotificationItemResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<NotificationItemResponse> response =
                PageResponse.from(
                        notificationService
                                .getSentNotifications(userId, page, size)
                                .map(NotificationItemResponse::from),
                        page,
                        size);

        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.NOTIFICATION_LIST_SUCCESS, response));
    }
}
