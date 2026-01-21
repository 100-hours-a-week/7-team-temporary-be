package molip.server.auth.store;

import java.util.Set;

public interface DeviceStore {

  void addDevice(Long userId, String deviceId);

  Set<String> listDevices(Long userId);

  void clearDevices(Long userId);
}
