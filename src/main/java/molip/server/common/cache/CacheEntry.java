package molip.server.common.cache;

public record CacheEntry<T>(long version, T payload) {}
