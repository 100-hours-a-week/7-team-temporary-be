package molip.server.auth.store.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisDeviceStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private SetOperations<String, String> setOperations;

    @InjectMocks private RedisDeviceStore redisDeviceStore;

    @Test
    void 디바이스를_추가하고_목록을_조회할_수_있다() {
        // given
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.members("auth:devices:1")).willReturn(Set.of("device-1"));

        // when
        redisDeviceStore.addDevice(1L, "device-1");
        Set<String> devices = redisDeviceStore.listDevices(1L);

        // then
        then(setOperations).should().add("auth:devices:1", "device-1");
        assertThat(devices).containsExactly("device-1");
    }
}
