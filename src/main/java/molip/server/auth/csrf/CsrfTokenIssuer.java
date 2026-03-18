package molip.server.auth.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CsrfTokenIssuer {

    private final CookieCsrfTokenRepository csrfTokenRepository;

    public void issue(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
    }
}
