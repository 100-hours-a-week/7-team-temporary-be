package molip.server.auth.store;

public interface TokenBlacklistStore {

  void add(Long userId, String token);

  boolean contains(long userId, String token);

  boolean contains(String token);
}
