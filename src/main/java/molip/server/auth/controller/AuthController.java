package molip.server.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import molip.server.auth.dto.request.LoginRequest;
import molip.server.auth.dto.response.AccessTokenResponse;
import molip.server.auth.dto.response.AuthResponse;
import molip.server.auth.service.AuthService;
import molip.server.common.SuccessCode;
import molip.server.common.response.ServerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String DEVICE_ID_COOKIE = "deviceId";

    private final AuthService authService;
    private final CookieCsrfTokenRepository csrfTokenRepository;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthController(
            AuthService authService,
            CookieCsrfTokenRepository csrfTokenRepository,
            @Value("${jwt.access-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {
        this.authService = authService;
        this.csrfTokenRepository = csrfTokenRepository;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @PostMapping("/token")
    @Override
    public ResponseEntity<ServerResponse<AccessTokenResponse>> login(
            @RequestBody LoginRequest request,
            @CookieValue(name = DEVICE_ID_COOKIE, required = false) String deviceId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        AuthResponse tokens = authService.login(request, deviceId);
        AccessTokenResponse response = new AccessTokenResponse(tokens.accessToken());

        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());
        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie deviceCookie = buildDeviceCookie(tokens.deviceId());

        issueCsrfToken(httpServletRequest, httpServletResponse);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
                .body(ServerResponse.success(SuccessCode.LOGIN_SUCCESS, response));
    }

    @DeleteMapping("/token")
    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String accessToken = resolveTokenFromCookie(request);
        authService.logout(accessToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expireCookie(ACCESS_TOKEN_COOKIE).toString())
                .header(HttpHeaders.SET_COOKIE, expireCookie(REFRESH_TOKEN_COOKIE).toString())
                .header(HttpHeaders.SET_COOKIE, expireCookie(DEVICE_ID_COOKIE).toString())
                .build();
    }

    @PutMapping("/token")
    @Override
    public ResponseEntity<ServerResponse<AccessTokenResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        AuthResponse tokens = authService.reissue(refreshToken);
        AccessTokenResponse response = new AccessTokenResponse(tokens.accessToken());

        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());
        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        ResponseCookie deviceCookie = buildDeviceCookie(tokens.deviceId());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deviceCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ServerResponse.success(SuccessCode.TOKEN_REISSUE_SUCCESS, response));
    }

    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpirationMs))
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                .build();
    }

    private ResponseCookie buildDeviceCookie(String deviceId) {
        return ResponseCookie.from(DEVICE_ID_COOKIE, deviceId)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                .build();
    }

    private ResponseCookie expireCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    private void issueCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
    }
}
