package molip.server.auth.store;

public interface TokenVersionStore {

    long getOrInit(Long userId);

    long get(Long userId);

    long increment(Long userId);
}
