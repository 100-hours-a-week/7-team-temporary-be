package molip.server.auth.store;

import java.util.Set;

public interface RefreshTokenStore {

  void save(Long userId, String deviceId, String refreshHash);

  boolean matches(Long userId, String deviceId, String refreshHash);

  void deleteAll(Long userId, Set<String> deviceIds);
}
