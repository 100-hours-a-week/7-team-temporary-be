package molip.server.auth.store.inmemory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import molip.server.auth.store.RefreshTokenStore;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

  private final ConcurrentHashMap<String, String> refreshTokens = new ConcurrentHashMap<>();

  @Override
  public void save(Long userId, String deviceId, String refreshHash) {
    refreshTokens.put(key(userId, deviceId), refreshHash);
  }

  @Override
  public boolean matches(Long userId, String deviceId, String refreshHash) {
    String stored = refreshTokens.get(key(userId, deviceId));
    return stored != null && stored.equals(refreshHash);
  }

  @Override
  public void deleteAll(Long userId, Set<String> deviceIds) {
    for (String deviceId : deviceIds) {
      refreshTokens.remove(key(userId, deviceId));
    }
  }

  private String key(Long userId, String deviceId) {
    return userId + ":" + deviceId;
  }
}
