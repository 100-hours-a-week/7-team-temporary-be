package molip.server.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import molip.server.notification.dto.request.UserFcmTokenDeactivateRequest;
import molip.server.notification.dto.request.UserFcmTokenUpsertRequest;
import molip.server.notification.facade.UserFcmTokenCommandFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserFcmTokenController implements UserFcmTokenApi {

    private final UserFcmTokenCommandFacade userFcmTokenCommandFacade;

    @PostMapping("/fcm-tokens")
    @Override
    public ResponseEntity<Void> upsertToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserFcmTokenUpsertRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        userFcmTokenCommandFacade.upsertToken(userId, request.fcmToken(), request.platform());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/fcm-tokens")
    @Override
    public ResponseEntity<Void> deactivateToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserFcmTokenDeactivateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername());

        userFcmTokenCommandFacade.deactivateToken(userId, request.fcmToken());

        return ResponseEntity.noContent().build();
    }
}
