package molip.server.auth.jwt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryTokenBlacklistStore implements TokenBlacklistStore {

  private final Map<Long, String> blackList;

  InMemoryTokenBlacklistStore() {
    this.blackList = new ConcurrentHashMap<>();
  }

  @Override
  public void add(Long userId, String token) {
    blackList.put(userId, token);
  }

  @Override
  public boolean contains(long userId, String token) {
    String blackListedToken = blackList.get(userId);
    return blackListedToken != null && blackListedToken.equals(token);
  }

  @Override
  public boolean contains(String token) {
    return blackList.values().stream().anyMatch(blackedToken -> blackedToken.equals(token));
  }
}
