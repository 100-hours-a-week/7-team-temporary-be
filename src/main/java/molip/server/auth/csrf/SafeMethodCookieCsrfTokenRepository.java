package molip.server.auth.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;

public class SafeMethodCookieCsrfTokenRepository implements CsrfTokenRepository {

    private static final Set<String> NULL_DELETE_ALLOW_DELETE_PATHS = Set.of("/token", "/users");

    private final CookieCsrfTokenRepository delegate =
            CookieCsrfTokenRepository.withHttpOnlyFalse();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }

    @Override
    public void saveToken(
            CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null && shouldBlockDeletion(request)) {
            return;
        }
        delegate.saveToken(token, request, response);
    }

    @Override
    public DeferredCsrfToken loadDeferredToken(
            HttpServletRequest request, HttpServletResponse response) {
        return delegate.loadDeferredToken(request, response);
    }

    private boolean shouldBlockDeletion(HttpServletRequest request) {
        if (HttpMethod.DELETE.matches(request.getMethod())) {
            String path = request.getServletPath();
            return !NULL_DELETE_ALLOW_DELETE_PATHS.contains(path);
        }
        return true;
    }
}
