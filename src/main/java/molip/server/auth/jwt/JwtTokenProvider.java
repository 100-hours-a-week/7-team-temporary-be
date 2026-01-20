package molip.server.auth.jwt;

import lombok.RequiredArgsConstructor;
import molip.server.auth.jwt.userDetails.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService userDetailsService;
  private final TokenBlacklistStore tokenBlacklistStore;

  public Authentication getAuthentication(String token) {
    Long userId = jwtUtil.extractUserId(token);

    UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  public boolean validateAccessToken(String token) {
    return isValidToken(token);
  }

  public boolean validateRefreshToken(String token) {
    return isValidToken(token);
  }

  private boolean isValidToken(String token) {
    if (token == null) {
      return false;
    }

    Long userId = jwtUtil.extractUserId(token);

    boolean isBlackList = tokenBlacklistStore.contains(userId, token);
    boolean isExpired = jwtUtil.isExpired(token);

    return !isBlackList && !isExpired;
  }
}
