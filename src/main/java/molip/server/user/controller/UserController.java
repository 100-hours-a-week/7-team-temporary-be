package molip.server.user.controller;

import java.time.Duration;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AuthResponse;
import molip.server.auth.service.AuthService;
import molip.server.common.SuccessCode;
import molip.server.common.response.PageResponse;
import molip.server.common.response.ServerResponse;
import molip.server.user.dto.request.SignUpRequest;
import molip.server.user.dto.request.UpdatePasswordRequest;
import molip.server.user.dto.request.UpdateProfileImageRequest;
import molip.server.user.dto.request.UpdateUserRequest;
import molip.server.user.dto.response.DuplicatedResponse;
import molip.server.user.dto.response.SignUpResponse;
import molip.server.user.dto.response.UserProfileResponse;
import molip.server.user.dto.response.UserSearchItemResponse;
import molip.server.user.entity.Users;
import molip.server.user.facade.UserQueryFacade;
import molip.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserApi {

    private final UserService userService;
    private final UserQueryFacade userQueryFacade;
    private final AuthService authService;
    private final long refreshTokenExpirationMs;

    public UserController(
            UserService userService,
            UserQueryFacade userQueryFacade,
            AuthService authService,
            @Value("${jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {
        this.userService = userService;
        this.userQueryFacade = userQueryFacade;
        this.authService = authService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @PostMapping("/users")
    @Override
    public ResponseEntity<ServerResponse<SignUpResponse>> signUp(
            @RequestBody SignUpRequest request,
            @CookieValue(name = "deviceId", required = false) String deviceId) {
        Users user =
                userService.registerUser(
                        request.email(),
                        request.password(),
                        request.nickname(),
                        request.gender(),
                        request.birth(),
                        request.focusTimeZone(),
                        request.dayEndTime(),
                        request.profileImageKey());

        AuthResponse tokens =
                authService.login(new LoginRequest(request.email(), request.password()), deviceId);
        SignUpResponse response = SignUpResponse.from(user.getId(), tokens.accessToken());
        ResponseCookie refreshCookie =
                ResponseCookie.from("refreshToken", tokens.refreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .path("/token")
                        .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                        .build();
        ResponseCookie deviceCookie =
                ResponseCookie.from("deviceId", tokens.deviceId())
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .path("/token")
                        .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
                .body(ServerResponse.success(SuccessCode.SIGNUP_SUCCESS, response));
    }

    @GetMapping("/users")
    @Override
    public ResponseEntity<ServerResponse<UserProfileResponse>> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        UserProfileResponse response = userQueryFacade.getUserProfile(userId);
        return ResponseEntity.ok(
                ServerResponse.success(SuccessCode.USER_PROFILE_FETCH_SUCCESS, response));
    }

    @GetMapping("/users/nickname")
    @Override
    public ResponseEntity<ServerResponse<PageResponse<UserSearchItemResponse>>> searchByNickname(
            @RequestParam String nickname,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/users/email")
    @Override
    public ResponseEntity<ServerResponse<DuplicatedResponse>> checkEmail(
            @RequestParam String email) {
        boolean isDuplicated = userService.checkEmailDuplicated(email);
        return ResponseEntity.ok(
                ServerResponse.success(
                        SuccessCode.EMAIL_DUPLICATION_CHECKED,
                        DuplicatedResponse.from(isDuplicated)));
    }

    @PatchMapping("/users")
    @Override
    public ResponseEntity<Void> update(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/image")
    @Override
    public ResponseEntity<Void> updateProfileImage(@RequestBody UpdateProfileImageRequest request) {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/password")
    @Override
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
        // userService.modifyPassword();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users")
    @Override
    public ResponseEntity<Void> delete() {
        return ResponseEntity.noContent().build();
    }
}
