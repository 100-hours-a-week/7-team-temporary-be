package molip.server.auth.store.inmemory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import molip.server.auth.store.DeviceStore;
import org.springframework.stereotype.Component;

@Component
public class InMemoryDeviceStore implements DeviceStore {

    private final ConcurrentHashMap<Long, Set<String>> devices = new ConcurrentHashMap<>();

    @Override
    public void addDevice(Long userId, String deviceId) {
        devices.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(deviceId);
    }

    @Override
    public Set<String> listDevices(Long userId) {
        return devices.getOrDefault(userId, Collections.emptySet());
    }

    @Override
    public void clearDevices(Long userId) {
        devices.remove(userId);
    }
}
