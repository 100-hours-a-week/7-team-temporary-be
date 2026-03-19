package molip.server.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import molip.server.auth.csrf.CsrfTokenIssuer;
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
import molip.server.user.facade.UserCommandFacade;
import molip.server.user.facade.UserQueryFacade;
import molip.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String DEVICE_ID_COOKIE = "deviceId";

    private final UserService userService;
    private final UserCommandFacade userCommandFacade;
    private final UserQueryFacade userQueryFacade;
    private final AuthService authService;
    private final CsrfTokenIssuer csrfTokenIssuer;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public UserController(
            UserService userService,
            UserCommandFacade userCommandFacade,
            UserQueryFacade userQueryFacade,
            AuthService authService,
            CsrfTokenIssuer csrfTokenIssuer,
            @Value("${jwt.access-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {
        this.userService = userService;
        this.userCommandFacade = userCommandFacade;
        this.userQueryFacade = userQueryFacade;
        this.authService = authService;
        this.csrfTokenIssuer = csrfTokenIssuer;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @PostMapping("/users")
    @Override
    public ResponseEntity<ServerResponse<SignUpResponse>> signUp(
            @RequestBody SignUpRequest request,
            @CookieValue(name = DEVICE_ID_COOKIE, required = false) String deviceId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        Users user =
                userService.registerUser(
                        request.email(),
                        request.password(),
                        request.nickname(),
                        request.gender(),
                        request.birth(),
                        request.focusTimeZone(),
                        request.dayEndTime(),
                        request.profileImageKey(),
                        request.terms());

        AuthResponse tokens =
                authService.login(new LoginRequest(request.email(), request.password()), deviceId);

        SignUpResponse response = SignUpResponse.from(user.getId(), tokens.accessToken());

        ResponseCookie accessCookie =
                ResponseCookie.from(ACCESS_TOKEN_COOKIE, tokens.accessToken())
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofMillis(accessTokenExpirationMs))
                        .build();

        ResponseCookie refreshCookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, tokens.refreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                        .build();

        ResponseCookie deviceCookie =
                ResponseCookie.from(DEVICE_ID_COOKIE, tokens.deviceId())
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                        .build();
        csrfTokenIssuer.issue(httpServletRequest, httpServletResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String nickname,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());

        PageResponse<UserSearchItemResponse> response =
                userQueryFacade.searchByNickname(userId, nickname, page, size);

        return ResponseEntity.ok(ServerResponse.success(SuccessCode.USER_SEARCH_SUCCESS, response));
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
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        userService.modifyUserDetails(
                userId,
                request.gender(),
                request.birth(),
                request.focusTimeZone(),
                request.dayEndTime(),
                request.nickname());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/image")
    @Override
    public ResponseEntity<Void> updateProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileImageRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        userCommandFacade.changeProfileImage(userId, request.imageKey());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/password")
    @Override
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdatePasswordRequest request) {
        Long userId = Long.valueOf(userDetails.getUsername());

        userService.modifyPassword(userId, request.newPassword());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users")
    @Override
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }
}
