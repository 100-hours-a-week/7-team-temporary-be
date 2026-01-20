package molip.server.auth.jwt;

public interface TokenBlacklistStore {
  void add(Long userId, String token);

  boolean contains(long userId, String token);

  boolean contains(String token);
}
