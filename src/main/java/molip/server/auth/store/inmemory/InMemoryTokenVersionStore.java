package molip.server.auth.store.inmemory;

import java.util.concurrent.ConcurrentHashMap;
import molip.server.auth.store.TokenVersionStore;
import org.springframework.stereotype.Component;

@Component
public class InMemoryTokenVersionStore implements TokenVersionStore {

    private final ConcurrentHashMap<Long, Long> versions = new ConcurrentHashMap<>();

    @Override
    public long getOrInit(Long userId) {
        return versions.computeIfAbsent(userId, key -> 1L);
    }

    @Override
    public long get(Long userId) {
        return versions.getOrDefault(userId, 1L);
    }

    @Override
    public long increment(Long userId) {
        return versions.merge(userId, 1L, Long::sum);
    }
}
